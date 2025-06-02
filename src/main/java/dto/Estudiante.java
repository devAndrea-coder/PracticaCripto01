/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dto;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author ANDREA
 */
@Entity
@Table(name = "estudiante")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Estudiante.findAll", query = "SELECT e FROM Estudiante e"),
    @NamedQuery(name = "Estudiante.findByCodiEstdWeb", query = "SELECT e FROM Estudiante e WHERE e.codiEstdWeb = :codiEstdWeb"),
    @NamedQuery(name = "Estudiante.findByNdniEstdWeb", query = "SELECT e FROM Estudiante e WHERE e.ndniEstdWeb = :ndniEstdWeb"),
    @NamedQuery(name = "Estudiante.findByAppaEstdWeb", query = "SELECT e FROM Estudiante e WHERE e.appaEstdWeb = :appaEstdWeb"),
    @NamedQuery(name = "Estudiante.findByApmaEstdWeb", query = "SELECT e FROM Estudiante e WHERE e.apmaEstdWeb = :apmaEstdWeb"),
    @NamedQuery(name = "Estudiante.findByNombEstdWeb", query = "SELECT e FROM Estudiante e WHERE e.nombEstdWeb = :nombEstdWeb"),
    @NamedQuery(name = "Estudiante.findByFechNaciEstdWeb", query = "SELECT e FROM Estudiante e WHERE e.fechNaciEstdWeb = :fechNaciEstdWeb"),
    @NamedQuery(name = "Estudiante.findByLogiEstd", query = "SELECT e FROM Estudiante e WHERE e.logiEstd = :logiEstd"),
    @NamedQuery(name = "Estudiante.findByPassEstd", query = "SELECT e FROM Estudiante e WHERE e.passEstd = :passEstd")})
public class Estudiante implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "codiEstdWeb")
    private Integer codiEstdWeb;
    @Size(max = 50)
    @Column(name = "ndniEstdWeb")
    private String ndniEstdWeb;
    @Size(max = 50)
    @Column(name = "appaEstdWeb")
    private String appaEstdWeb;
    @Size(max = 50)
    @Column(name = "apmaEstdWeb")
    private String apmaEstdWeb;
    @Size(max = 50)
    @Column(name = "nombEstdWeb")
    private String nombEstdWeb;
    @Column(name = "fechNaciEstdWeb")
    @Temporal(TemporalType.DATE)
    private Date fechNaciEstdWeb;
    @Size(max = 100)
    @Column(name = "logiEstd")
    private String logiEstd;
    @Size(max = 500)
    @Column(name = "passEstd")
    private String passEstd;

    public Estudiante() {
    }

    public Estudiante(Integer codiEstdWeb) {
        this.codiEstdWeb = codiEstdWeb;
    }

    public Integer getCodiEstdWeb() {
        return codiEstdWeb;
    }

    public void setCodiEstdWeb(Integer codiEstdWeb) {
        this.codiEstdWeb = codiEstdWeb;
    }

    public String getNdniEstdWeb() {
        return ndniEstdWeb;
    }

    public void setNdniEstdWeb(String ndniEstdWeb) {
        this.ndniEstdWeb = ndniEstdWeb;
    }

    public String getAppaEstdWeb() {
        return appaEstdWeb;
    }

    public void setAppaEstdWeb(String appaEstdWeb) {
        this.appaEstdWeb = appaEstdWeb;
    }

    public String getApmaEstdWeb() {
        return apmaEstdWeb;
    }

    public void setApmaEstdWeb(String apmaEstdWeb) {
        this.apmaEstdWeb = apmaEstdWeb;
    }

    public String getNombEstdWeb() {
        return nombEstdWeb;
    }

    public void setNombEstdWeb(String nombEstdWeb) {
        this.nombEstdWeb = nombEstdWeb;
    }

    public Date getFechNaciEstdWeb() {
        return fechNaciEstdWeb;
    }

    public void setFechNaciEstdWeb(Date fechNaciEstdWeb) {
        this.fechNaciEstdWeb = fechNaciEstdWeb;
    }

    public String getLogiEstd() {
        return logiEstd;
    }

    public void setLogiEstd(String logiEstd) {
        this.logiEstd = logiEstd;
    }

    public String getPassEstd() {
        return passEstd;
    }

    public void setPassEstd(String passEstd) {
        this.passEstd = passEstd;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (codiEstdWeb != null ? codiEstdWeb.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Estudiante)) {
            return false;
        }
        Estudiante other = (Estudiante) object;
        if ((this.codiEstdWeb == null && other.codiEstdWeb != null) || (this.codiEstdWeb != null && !this.codiEstdWeb.equals(other.codiEstdWeb))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "dto.Estudiante[ codiEstdWeb=" + codiEstdWeb + " ]";
    }
    
}
