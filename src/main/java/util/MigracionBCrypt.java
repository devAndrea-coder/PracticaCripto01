package util;

/**
 *
 * @author ANDREA
 */

import dao.EstudianteJpaController;
import dto.Estudiante;
import org.mindrot.jbcrypt.BCrypt;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.List;

public class MigracionBCrypt {
    //HASH DE LOS DATOS EN BD
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("com.mycompany_CriptoPractica_war_1.0-SNAPSHOTPU");
        EstudianteJpaController controller = new EstudianteJpaController(emf);
        
        try {
            // Obtener todos los estudiantes
            List<Estudiante> estudiantes = controller.findEstudianteEntities();
            
            System.out.println("Iniciando migración de " + estudiantes.size() + " estudiantes...");
            
            int migrados = 0;
            for (Estudiante estudiante : estudiantes) {
                String passwordActual = estudiante.getPassEstd();
                
                // Verificar si ya está hasheada (BCrypt hashes empiezan con $2a$, $2b$, etc.)
                if (!passwordActual.startsWith("$2a$") && !passwordActual.startsWith("$2b$") && !passwordActual.startsWith("$2y$")) {
                    // La contraseña está en texto plano, necesita ser hasheada
                    String hashedPassword = BCrypt.hashpw(passwordActual, BCrypt.gensalt(12));
                    estudiante.setPassEstd(hashedPassword);
                    
                    // Actualizar en la base de datos
                    controller.edit(estudiante);
                    migrados++;
                    
                    System.out.println("Migrado DNI: " + estudiante.getNdniEstdWeb());
                } else {
                    System.out.println("Ya hasheado DNI: " + estudiante.getNdniEstdWeb());
                }
            }
            
            System.out.println("Migración completada. " + migrados + " contraseñas fueron hasheadas.");
            
        } catch (Exception e) {
            System.err.println("Error durante la migración: " + e.getMessage());
            e.printStackTrace();
        } finally {
            emf.close();
        }
    }
}

