package entite;

import java.sql.Date;
import gno.*;
@InfoDb(name="Photos")
public class Photos extends Motherobj<Photos>
{ 
  @Id
  int idphotos;
  String nomphoto;
  byte[] photocode;  
  public int getIdphotos() {
    return idphotos;
  }
  public void setIdphotos(int idphotos) {
    this.idphotos = idphotos;
  }
  public String getNomphoto() {
    return nomphoto;
  }
  public void setNomphoto(String nomphoto) {
    this.nomphoto = nomphoto;
  }
  public byte[] getPhotocode() {
    return photocode;
  }
  public void setPhotocode(byte[] photocode) {
    this.photocode = photocode;
  }
}