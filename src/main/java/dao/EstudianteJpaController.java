package dao;

import dao.exceptions.NonexistentEntityException;
import dto.Estudiante;
import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 *
 * @author ANDREA
 */
public class EstudianteJpaController implements Serializable {

    public EstudianteJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Estudiante estudiante) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(estudiante);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Estudiante estudiante) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            estudiante = em.merge(estudiante);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = estudiante.getCodiEstdWeb();
                if (findEstudiante(id) == null) {
                    throw new NonexistentEntityException("The estudiante with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Estudiante estudiante;
            try {
                estudiante = em.getReference(Estudiante.class, id);
                estudiante.getCodiEstdWeb();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The estudiante with id " + id + " no longer exists.", enfe);
            }
            em.remove(estudiante);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Estudiante> findEstudianteEntities() {
        return findEstudianteEntities(true, -1, -1);
    }

    public List<Estudiante> findEstudianteEntities(int maxResults, int firstResult) {
        return findEstudianteEntities(false, maxResults, firstResult);
    }

    private List<Estudiante> findEstudianteEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Estudiante.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Estudiante findEstudiante(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Estudiante.class, id);
        } finally {
            em.close();
        }
    }

    public int getEstudianteCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Estudiante> rt = cq.from(Estudiante.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
    // METODO VALIDAR POR DNI
    public Estudiante validarEstudianteDni(String dni, String pass) {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createNamedQuery("Estudiante.validarDni");
            q.setParameter("ndniEstdWeb", dni);
            q.setParameter("passEstd", pass);

            List<Estudiante> lista = q.getResultList();

            if (lista == null || lista.isEmpty()) {
                return null;
            } else {
                return lista.get(0);
            }
        } finally {
            em.close();
        }
    }
    
    //BUSQUEDA POR DNI
    public Estudiante buscarPorDniSolo(String dni) {
        EntityManager em = getEntityManager();
        try {
            Query query = em.createQuery("Estudiante.findByNdniEstdWeb");
            query.setParameter("dni", dni);

            List<Estudiante> resultados = query.getResultList();

            if (resultados.isEmpty()) {
                return null;
            }

            return resultados.get(0);

        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            System.err.println("Error al buscar estudiante por DNI: " + e.getMessage());
            return null;
        } finally {
            em.close();
        }
    }
    
    //REGISTRAR NUEVO ESTUDIANTE
    public boolean registrarEstudianteConHash(Estudiante estudiante, String plainPassword) {
        try {
            // Hashear la contraseña antes de guardar
            String hashedPassword = org.mindrot.jbcrypt.BCrypt.hashpw(plainPassword, org.mindrot.jbcrypt.BCrypt.gensalt(12));
            estudiante.setPassEstd(hashedPassword);

            // Guardar en la base de datos
            create(estudiante);
            return true;

        } catch (Exception e) {
            System.err.println("Error al registrar estudiante: " + e.getMessage());
            return false;
        }
    }
}
