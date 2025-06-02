package servlet;

import dao.EstudianteJpaController;
import dto.Estudiante;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;

/**
 *
 * @author ANDREA
 */
@WebServlet(name = "cambiarContrasena", urlPatterns = {"/contrasena"})
public class cambiarContrasena extends HttpServlet {
    private EstudianteJpaController estudianteService;
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("com.mycompany_CriptoPractica_war_1.0-SNAPSHOTPU");

    @Override
    public void init() throws ServletException {
        super.init();
        estudianteService = new EstudianteJpaController(emf);
    }

    // CONFIGURAR HEADERS CORS - CORREGIDO
    private void configurarCORS(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        configurarCORS(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        configurarCORS(response);
        PrintWriter out = response.getWriter();

        try {
            // OBTENER PARÁMETROS - MEJORADO CON LOGS
            String login = request.getParameter("login");
            String claveActual = request.getParameter("claveActual");
            String nuevaClave = request.getParameter("nuevaClave");

            // AGREGAR LOGS PARA DEBUGGING
            System.out.println("=== CAMBIO DE CONTRASEÑA ===");
            System.out.println("Login recibido: " + login);
            System.out.println("Clave actual recibida: " + (claveActual != null ? "***" : "null"));
            System.out.println("Nueva clave recibida: " + (nuevaClave != null ? "***" : "null"));
            System.out.println("Content-Type: " + request.getContentType());

            // VALIDAR CAMPOS REQUERIDOS
            if (login == null || login.trim().isEmpty()
                    || claveActual == null || claveActual.trim().isEmpty()
                    || nuevaClave == null || nuevaClave.trim().isEmpty()) {

                System.out.println("ERROR: Campos faltantes");
                JSONObject errorJson = new JSONObject();
                errorJson.put("success", false);
                errorJson.put("message", "Todos los campos son obligatorios");
                out.print(errorJson.toString());
                out.flush();
                return;
            }

            // BUSCAR ESTUDIANTE POR LOGIN
            List<Estudiante> estudiantes = estudianteService.findEstudianteEntities();
            Estudiante estudiante = null;

            for (Estudiante e : estudiantes) {
                if (e.getLogiEstd() != null && e.getLogiEstd().equals(login.trim())) {
                    estudiante = e;
                    break;
                }
            }

            if (estudiante == null) {
                System.out.println("ERROR: Usuario no encontrado - " + login);
                JSONObject errorJson = new JSONObject();
                errorJson.put("success", false);
                errorJson.put("message", "Usuario no encontrado");
                out.print(errorJson.toString());
                out.flush();
                return;
            }

            System.out.println("Usuario encontrado: " + estudiante.getLogiEstd());

            // VERIFICAR CONTRASEÑA ACTUAL CON BCRYPT
            String hashAlmacenado = estudiante.getPassEstd();
            System.out.println("Verificando contraseña con bcrypt...");
            
            if (!BCrypt.checkpw(claveActual.trim(), hashAlmacenado)) {
                System.out.println("ERROR: Contraseña actual incorrecta");
                JSONObject errorJson = new JSONObject();
                errorJson.put("success", false);
                errorJson.put("message", "La contraseña actual es incorrecta");
                out.print(errorJson.toString());
                out.flush();
                return;
            }

            // VALIDAR QUE LA NUEVA CONTRASEÑA SEA DIFERENTE
            if (BCrypt.checkpw(nuevaClave.trim(), hashAlmacenado)) {
                System.out.println("ERROR: Nueva contraseña igual a la actual");
                JSONObject errorJson = new JSONObject();
                errorJson.put("success", false);
                errorJson.put("message", "La nueva contraseña debe ser diferente a la actual");
                out.print(errorJson.toString());
                out.flush();
                return;
            }

            // HASHEAR LA NUEVA CONTRASEÑA CON BCRYPT
            System.out.println("Hasheando nueva contraseña...");
            String nuevaClaveHash = BCrypt.hashpw(nuevaClave.trim(), BCrypt.gensalt(12));
            
            // ACTUALIZAR CONTRASEÑA CON EL HASH
            estudiante.setPassEstd(nuevaClaveHash);
            estudianteService.edit(estudiante);

            System.out.println("Contraseña actualizada exitosamente para: " + login);

            JSONObject successJson = new JSONObject();
            successJson.put("success", true);
            successJson.put("message", "Contraseña cambiada exitosamente");
            successJson.put("requireRelogin", true); // Indicar que debe volver a iniciar sesión
            out.print(successJson.toString());
            out.flush();

        } catch (Exception e) {
            System.out.println("ERROR INTERNO: " + e.getMessage());
            e.printStackTrace();

            JSONObject errorJson = new JSONObject();
            errorJson.put("success", false);
            errorJson.put("message", "Error interno del servidor: " + e.getMessage());
            out.print(errorJson.toString());
            out.flush();
        }
    }
}
