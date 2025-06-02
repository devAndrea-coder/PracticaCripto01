package servlet;

import dto.Estudiante;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;

/**
 *
 * @author Quichiz
 */
@WebServlet(name = "estudiante", urlPatterns = {"/estudiante"})
public class estudiante extends HttpServlet {
   // FORMATO DE FECHA PARA CONVERSIÓN
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    // CONFIGURACIÓN DE PERSISTENCIA JPA
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("com.mycompany_CriptoPractica_war_1.0-SNAPSHOTPU");

    // OBTENER ENTITY MANAGER
    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    // MÉTODO PARA HASHEAR CONTRASEÑAS CON BCRYPT
    private String hashPassword(String password) {
        try {
            // Generar salt y hashear contraseña con BCrypt
            return BCrypt.hashpw(password, BCrypt.gensalt(12));
        } catch (Exception e) {
            throw new RuntimeException("Error al hashear contraseña con BCrypt", e);
        }
    }
    
    // MÉTODO PARA VERIFICAR CONTRASEÑAS CON BCRYPT
    private boolean verifyPassword(String password, String hashedPassword) {
        try {
            return BCrypt.checkpw(password, hashedPassword);
        } catch (Exception e) {
            System.err.println("Error al verificar contraseña: " + e.getMessage());
            return false;
        }
    }

    // MÉTODO GET - LISTAR TODOS LOS ESTUDIANTES
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        EntityManager em = getEntityManager();
        JSONObject jsonResponse = new JSONObject();

        try {
            List<Estudiante> estudiantes = em.createQuery("SELECT e FROM Estudiante e ORDER BY e.codiEstdWeb ASC", Estudiante.class).getResultList();
            JSONArray jsonArray = new JSONArray();

            for (Estudiante e : estudiantes) {
                JSONObject obj = new JSONObject();
                obj.put("codiEstdWeb", e.getCodiEstdWeb());
                obj.put("ndniEstdWeb", e.getNdniEstdWeb());
                obj.put("appaEstdWeb", e.getAppaEstdWeb());
                obj.put("apmaEstdWeb", e.getApmaEstdWeb());
                obj.put("nombEstdWeb", e.getNombEstdWeb());

                // FORMATEAR FECHA
                if (e.getFechNaciEstdWeb() != null) {
                    obj.put("fechNaciEstdWeb", dateFormat.format(e.getFechNaciEstdWeb()));
                } else {
                    obj.put("fechNaciEstdWeb", JSONObject.NULL);
                }

                // VALIDACIÓN SIMPLE PARA CAMPOS STRING
                obj.put("logiEstd", e.getLogiEstd() != null ? e.getLogiEstd() : "");
                // NO ENVIAR LA CONTRASEÑA AL FRONTEND POR SEGURIDAD
                obj.put("passEstd", ""); // SIEMPRE VACÍO EN GET

                jsonArray.put(obj);
            }

            jsonResponse.put("success", true);
            jsonResponse.put("data", jsonArray);
            jsonResponse.put("message", "Estudiantes cargados correctamente");

        } catch (Exception e) {
            jsonResponse.put("success", false);
            jsonResponse.put("message", "Error al cargar estudiantes: " + e.getMessage());
        } finally {
            em.close();
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.print(jsonResponse.toString());
        out.flush();
    }

    // MÉTODO POST - CREAR NUEVO ESTUDIANTE
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        EntityManager em = getEntityManager();
        JSONObject jsonResponse = new JSONObject();

        try {
            // LEER DATOS JSON DEL REQUEST
            BufferedReader reader = request.getReader();
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            JSONObject json = new JSONObject(jsonBuilder.toString());

            // VALIDAR CAMPOS REQUERIDOS
            String[] camposRequeridos = {"ndniEstdWeb", "appaEstdWeb", "apmaEstdWeb", "nombEstdWeb", "fechNaciEstdWeb", "logiEstd", "passEstd"};
            for (String campo : camposRequeridos) {
                if (!json.has(campo) || json.getString(campo).trim().isEmpty()) {
                    jsonResponse.put("success", false);
                    jsonResponse.put("message", "El campo " + campo + " es requerido");
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    sendResponse(response, jsonResponse);
                    return;
                }
            }

            // VALIDAR QUE EL LOGIN NO EXISTA
            List<Estudiante> existeLogin = em.createQuery("SELECT e FROM Estudiante e WHERE e.logiEstd = :login", Estudiante.class)
                    .setParameter("login", json.getString("logiEstd"))
                    .getResultList();

            if (!existeLogin.isEmpty()) {
                jsonResponse.put("success", false);
                jsonResponse.put("message", "El login ya existe, por favor elija otro");
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                sendResponse(response, jsonResponse);
                return;
            }

            // CREAR NUEVO ESTUDIANTE
            Estudiante estudiante = new Estudiante();
            estudiante.setNdniEstdWeb(json.getString("ndniEstdWeb"));
            estudiante.setAppaEstdWeb(json.getString("appaEstdWeb").toUpperCase());
            estudiante.setApmaEstdWeb(json.getString("apmaEstdWeb").toUpperCase());
            estudiante.setNombEstdWeb(json.getString("nombEstdWeb").toUpperCase());

            // CONVERTIR FECHA DE STRING A DATE
            String fechaStr = json.getString("fechNaciEstdWeb");
            Date fecha = dateFormat.parse(fechaStr);
            estudiante.setFechNaciEstdWeb(fecha);

            estudiante.setLogiEstd(json.getString("logiEstd"));
            
            // HASHEAR LA CONTRASEÑA CON BCRYPT ANTES DE GUARDAR
            String passwordPlano = json.getString("passEstd");
            String passwordHasheado = hashPassword(passwordPlano);
            estudiante.setPassEstd(passwordHasheado);

            System.out.println("DEBUG - Creando estudiante:");
            System.out.println("Login: " + json.getString("logiEstd"));
            System.out.println("Password original: " + passwordPlano);
            System.out.println("Password hasheado: " + passwordHasheado);

            // PERSISTIR EN BASE DE DATOS
            em.getTransaction().begin();
            em.persist(estudiante);
            em.getTransaction().commit();

            jsonResponse.put("success", true);
            jsonResponse.put("message", "Estudiante creado correctamente");
            response.setStatus(HttpServletResponse.SC_CREATED);

        } catch (Exception e) {
            // ROLLBACK EN CASO DE ERROR
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            jsonResponse.put("success", false);
            jsonResponse.put("message", "Error al crear estudiante: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        } finally {
            em.close();
        }

        sendResponse(response, jsonResponse);
    }

    // MÉTODO PUT - ACTUALIZAR ESTUDIANTE EXISTENTE
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        EntityManager em = getEntityManager();
        JSONObject jsonResponse = new JSONObject();

        try {
            // LEER DATOS JSON DEL REQUEST
            BufferedReader reader = request.getReader();
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            JSONObject json = new JSONObject(jsonBuilder.toString());
            
            if (!json.has("codiEstdWeb")) {
                jsonResponse.put("success", false);
                jsonResponse.put("message", "ID del estudiante es requerido para actualizar");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                sendResponse(response, jsonResponse);
                return;
            }

            int codiEstdWeb = json.getInt("codiEstdWeb");

            em.getTransaction().begin();

            // BUSCAR ESTUDIANTE POR ID
            Estudiante estudiante = em.find(Estudiante.class, codiEstdWeb);

            if (estudiante != null) {
                // VALIDAR QUE EL LOGIN NO EXISTA EN OTRO ESTUDIANTE
                List<Estudiante> existeLogin = em.createQuery("SELECT e FROM Estudiante e WHERE e.logiEstd = :login AND e.codiEstdWeb != :id", Estudiante.class)
                        .setParameter("login", json.getString("logiEstd"))
                        .setParameter("id", codiEstdWeb)
                        .getResultList();

                if (!existeLogin.isEmpty()) {
                    em.getTransaction().rollback();
                    jsonResponse.put("success", false);
                    jsonResponse.put("message", "El login ya existe en otro estudiante");
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    sendResponse(response, jsonResponse);
                    return;
                }

                // ACTUALIZAR DATOS
                estudiante.setNdniEstdWeb(json.getString("ndniEstdWeb"));
                estudiante.setAppaEstdWeb(json.getString("appaEstdWeb").toUpperCase());
                estudiante.setApmaEstdWeb(json.getString("apmaEstdWeb").toUpperCase());
                estudiante.setNombEstdWeb(json.getString("nombEstdWeb").toUpperCase());

                String fechaStr = json.getString("fechNaciEstdWeb");
                Date fecha = dateFormat.parse(fechaStr);
                estudiante.setFechNaciEstdWeb(fecha);

                estudiante.setLogiEstd(json.getString("logiEstd"));

                // SOLO ACTUALIZAR CONTRASEÑA SI SE PROPORCIONA UNA NUEVA
                String nuevaPassword = json.optString("passEstd", "").trim();
                if (!nuevaPassword.isEmpty()) {
                    String passwordHasheado = hashPassword(nuevaPassword);
                    estudiante.setPassEstd(passwordHasheado);
                    
                    System.out.println("DEBUG - Actualizando password:");
                    System.out.println("Login: " + json.getString("logiEstd"));
                    System.out.println("Nueva password: " + nuevaPassword);
                    System.out.println("Nueva password hasheada: " + passwordHasheado);
                }

                // GUARDAR CAMBIOS
                em.merge(estudiante);
                em.getTransaction().commit();

                jsonResponse.put("success", true);
                jsonResponse.put("message", "Estudiante actualizado correctamente");
            } else {
                em.getTransaction().rollback();
                jsonResponse.put("success", false);
                jsonResponse.put("message", "Estudiante no encontrado");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (Exception e) {
            // ROLLBACK EN CASO DE ERROR
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            jsonResponse.put("success", false);
            jsonResponse.put("message", "Error al actualizar estudiante: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        } finally {
            em.close();
        }

        sendResponse(response, jsonResponse);
    }

    // MÉTODO DELETE - ELIMINAR ESTUDIANTE
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        EntityManager em = getEntityManager();
        JSONObject jsonResponse = new JSONObject();

        try {
            // OBTENER ID DEL ESTUDIANTE A ELIMINAR
            String codiParam = request.getParameter("codiEstdWeb");
            if (codiParam == null || codiParam.trim().isEmpty()) {
                jsonResponse.put("success", false);
                jsonResponse.put("message", "ID del estudiante es requerido");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                sendResponse(response, jsonResponse);
                return;
            }

            int codiEstdWeb = Integer.parseInt(codiParam);

            em.getTransaction().begin();

            // BUSCAR ESTUDIANTE POR ID
            Estudiante estudiante = em.find(Estudiante.class, codiEstdWeb);

            if (estudiante != null) {
                // ELIMINAR ESTUDIANTE
                em.remove(estudiante);
                em.getTransaction().commit();

                jsonResponse.put("success", true);
                jsonResponse.put("message", "Estudiante eliminado correctamente");
            } else {
                em.getTransaction().rollback();
                jsonResponse.put("success", false);
                jsonResponse.put("message", "Estudiante no encontrado");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (NumberFormatException e) {
            jsonResponse.put("success", false);
            jsonResponse.put("message", "ID del estudiante debe ser un número válido");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            // ROLLBACK EN CASO DE ERROR
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            jsonResponse.put("success", false);
            jsonResponse.put("message", "Error al eliminar estudiante: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            em.close();
        }

        sendResponse(response, jsonResponse);
    }

    // MÉTODO PÚBLICO PARA VERIFICAR LOGIN (USADO POR SERVLET DE LOGIN)
    public boolean verificarLogin(String login, String password) {
        EntityManager em = getEntityManager();
        try {
            List<Estudiante> estudiantes = em.createQuery("SELECT e FROM Estudiante e WHERE e.logiEstd = :login", Estudiante.class)
                    .setParameter("login", login)
                    .getResultList();
            
            if (!estudiantes.isEmpty()) {
                Estudiante estudiante = estudiantes.get(0);
                String hashedPassword = estudiante.getPassEstd();
                
                System.out.println("DEBUG - Verificando login:");
                System.out.println("Login: " + login);
                System.out.println("Password ingresada: " + password);
                System.out.println("Hash almacenado: " + hashedPassword);
                
                boolean resultado = verifyPassword(password, hashedPassword);
                System.out.println("Resultado verificación: " + resultado);
                
                return resultado;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error en verificarLogin: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    // MÉTODO AUXILIAR PARA ENVIAR RESPUESTA
    private void sendResponse(HttpServletResponse response, JSONObject jsonResponse) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.print(jsonResponse.toString());
        out.flush();
    }

    // MANEJAR PETICIONES OPTIONS PARA CORS
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, X-Requested-With");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    public String getServletInfo() {
        return "Servlet para operaciones CRUD de estudiantes con contraseñas BCrypt - Versión segura";
    }

    @Override
    public void destroy() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
        super.destroy();
    }
}
