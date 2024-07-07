package entite;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

import gno.*;

@InfoDb(name="test4")
public class Test4 extends Motherobj<Test4>{
    @Id
    int id;
    Date daty ;
    Timestamp datyh;
    String nom;
    float nb;
    Time temp;

    public Test4() {
    }
    public Test4(int id, Date daty, Timestamp datyh, String nom, float nb, Time temp) {
        this.id = id;
        this.daty = daty;
        this.datyh = datyh;
        this.nom = nom;
        this.nb = nb;
        this.temp = temp;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public Date getDaty() {
        return daty;
    }
    public void setDaty(Date daty) {
        this.daty = daty;
    }
    public Timestamp getDatyh() {
        return datyh;
    }
    public void setDatyh(Timestamp datyh) {
        this.datyh = datyh;
    }
    public String getNom() {
        return nom;
    }
    public void setNom(String nom) {
        this.nom = nom;
    }
    public float getNb() {
        return nb;
    }
    public void setNb(float nb) {
        this.nb = nb;
    }
    public Time getTemp() {
        return temp;
    }
    public void setTemp(Time temp) {
        this.temp = temp;
    }

    
}
