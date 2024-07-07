package gno;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.lang.reflect.Array;
import java.sql.*;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.lang.model.element.Name;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import java.nio.file.Files;
import java.io.IOException;
import java.util.Base64;
public class GenericDao<T>
{ 
  Statement statement;
  Class infodbclass=InfoDb.class;
    public Statement getStatement(){    
        return statement;   
    }
    public void setStatement(Statement newStmt){     
        statement=newStmt; 
    }
  //---------------------------------------------------------------------------------------------------
    public void createStatementIfNull(Connection connection)throws Exception{
      if(statement==null){
        statement=connection.createStatement();
      }else if(statement.isClosed()==true){
        statement=connection.createStatement();
      }if(connection==null){
        throw new Exception("la connection est null");
      }
    }
//-----------------------------------------------------------------------------------------------------
    public void update(String sqlupdate,Connection connection)throws Exception{
      createStatementIfNull(connection);
      int nb=statement.executeUpdate(sqlupdate);
    }
    public void insert(String sqlinsert,Connection connection)throws Exception{
      createStatementIfNull(connection);
      int nb=statement.executeUpdate(sqlinsert);
    }
    public void delete(String sqldelete,Connection connection)throws Exception{
      createStatementIfNull(connection);
      int nb=statement.executeUpdate(sqldelete);
    }

    public void executeNoSelect(Connection connection, String sqlprepare, Object[] valuesobj) throws Exception {
      try (PreparedStatement statement = connection.prepareStatement(sqlprepare)) {
          setStatement(statement, valuesobj);
          statement.executeUpdate();
      }
    }

//----------------------------------------------------------------------------------------------
//     public int getSequence(String nom)throws Exception{
//       ResultSet resultSet=statement.executeQuery("select "+nom+".NEXTVAL from dual");
//       resultSet.next();
//       String s=resultSet.getString(1);
//       resultSet.close();
//       return Integer.valueOf(s);
//     }
//-----------------------------------------------------------------------------------------------
public String DatetoFormatDataBase(String sDate){
  SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
  return simpleDateFormat.format(Date.valueOf(sDate)).toString();
}
public String DatetoFormatDataBase(Date date){
  SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
  return simpleDateFormat.format(date).toString();
}
//------------------------------------------------------------------------------------------------------------------ANNOTATION
public boolean annotationExit(Class classObject,Class classAnnotation)throws Exception{
  Annotation annotation= classObject.getAnnotation(classAnnotation);
  if(annotation==null){ return true; }
  return false;
}
public Field getOneFieldAnnoteByAnnotation(T tobj,Class annotationClass)throws Exception{
  Field[] fields=tobj.getClass().getDeclaredFields();
  if(fields==null){ return null; }
  for(int i=0;i<fields.length;i++){
    if(fields[i].getAnnotation(annotationClass)!=null){
      return fields[i];
    }
  }
  return null;
}
public String getNameFieldAnnoteByAnnotation(T tobj,Class annotationClass)throws Exception{
  Field field=getOneFieldAnnoteByAnnotation(tobj, annotationClass);
  if(field==null){ return null; }
  return field.getName();
}
public Object getValueAnnotation(Object object, Class annotationClass, String nameMethod) throws Exception {
  Annotation annotation = object.getClass().getAnnotation(annotationClass);
  ////System.out.println(annotation+"<---");
  if (object instanceof Field) {
     Field field = (Field) object;
     annotation = field.getAnnotation(annotationClass);
  }else if( object instanceof Method ){
     Method method = (Method) object;
     annotation = method.getAnnotation(annotationClass);
  }else if( object instanceof Class ){
     Class theClass=(Class)object;
     annotation = theClass.getAnnotation(annotationClass);
  }
  // ////System.out.println(annotation.annotationType());
  if(annotation==null){ return null; }
  Method metd=annotation.getClass().getMethod(nameMethod);
  Object value=metd.invoke(annotation, (Object[])null);
  return value;
}
public String getInfoDbName(Object object)throws Exception{
    Object obj=getValueAnnotation(object, this.infodbclass , "name");
    if(obj==null){
      if (object instanceof Field) {
        Field field = (Field) object;
        return field.getName();
      }else if( object instanceof Class ){
        Class theClass=(Class)object;
        return theClass.getSimpleName();
      }else{
        return object.getClass().getSimpleName();
      }
    }
    return obj.toString();
}
public String getInfoDbNameFieldAnnoteByAnnotation(T tobj,Class annotationClass)throws Exception{
  Field field=getOneFieldAnnoteByAnnotation(tobj, annotationClass);
  if(field==null){ return null; }
  return getInfoDbName(field); //mety annoter izy
}
public boolean isClassAnnotedAndWithValue(Class classe,Class annotationClass,String nameMethod,String value)throws Exception{
   Annotation annotation=null;
   annotation=classe.getAnnotation(annotationClass);
   if(annotation==null){ return false; }
   Object valeur=getValueAnnotation(classe,annotationClass, nameMethod);
   String valeurS=(String)valeur;
   if(value.compareToIgnoreCase(valeurS)==0){ return true; }
   else{ return false; }
}
//----
public String toLowerCaseFirst(String str){ return str.toLowerCase().substring(0,1)+str.substring(1,str.length()); }
//-----------------------------------------------------------------------------------------------recupere les valeur d'une table
    public HashMap<String,String> columnsInDataBase(String nameTable,DatabaseMetaData metaData)throws Exception{
      ResultSet columns=metaData.getColumns(null, null,toLowerCaseFirst(nameTable) , null);
      HashMap<String,String> hashMap=new HashMap<String,String>();
      String strtemp="";
      while (columns.next()) {
        strtemp = columns.getString("COLUMN_NAME");
        //System.out.println(strtemp);
        hashMap.put(strtemp, strtemp);
      }
      columns.close();
      return hashMap;
    }
    public String[][] valueSelectToString(String sqlSelect,Connection connection)throws Exception{
      createStatementIfNull(connection);
      Vector vresultat=new Vector();
      ResultSet resultSet=statement.executeQuery(sqlSelect);//
      ResultSetMetaData rsmd=resultSet.getMetaData();
      int nbcolumn=rsmd.getColumnCount(); //nb colonne 

      boolean encore=resultSet.next(); //mbola misy ve le ligne
      String[] col=null;
      while(encore){//if==true
          col=new String[nbcolumn];
          for(int i=1;i<=nbcolumn;i++){
            col[i-1]=resultSet.getString(i); //l'element en String du colonne numero i dans ce ligne
          }
          vresultat.add(col);
          encore=resultSet.next();//resultSet.next() : ligne suivante / encore= false si plus de ligne
      }
      if(col==null){ return null; }
      String[][] resultat=new String[vresultat.size()][nbcolumn];
      for(int i=0;i<vresultat.size();i++){
        resultat[i]=(String[])vresultat.elementAt(i);
      }
      resultSet.close();
      return resultat;
    }
    private void setPreparedStatement(PreparedStatement statement,Object obj,int index)throws Exception{
      String fieldtype=obj.getClass().getSimpleName();
      if(fieldtype.equalsIgnoreCase("String")){
        String param=String.valueOf( obj );
        statement.setString(index, param);
      }else if(fieldtype.equalsIgnoreCase("int") || fieldtype.equalsIgnoreCase("INTEGER")){
        int param=Integer.valueOf( String.valueOf(obj) );
        statement.setInt(index, param);
      }else if(fieldtype.equalsIgnoreCase("long")){
        long param=Long.valueOf(String.valueOf(obj) );
        statement.setLong(index, param);
      }else if(fieldtype.equalsIgnoreCase("float")){
        float param=Float.valueOf(String.valueOf(obj) );
        statement.setFloat(index, param);
      }else if(fieldtype.equalsIgnoreCase("Double")){
        double param=Double.valueOf(String.valueOf(obj) );
        statement.setDouble(index, param);
      }else if(fieldtype.equalsIgnoreCase("char")){
        String param=String.valueOf(obj);
        statement.setString(index,param );
      }else if(fieldtype.equalsIgnoreCase("LocalDate")){
        LocalDate ldate=(LocalDate)obj;
        Date param=Date.valueOf( writeValueAccordingType("LocalDate", ldate) );
        statement.setDate(index, param);
      }else if(fieldtype.equalsIgnoreCase("Date")){
        Date param=(Date)obj;
        statement.setDate(index, param);
      }else if(fieldtype.equalsIgnoreCase("LocalDateTime")){
        LocalDateTime ldate=(LocalDateTime)obj;
        Timestamp param=Timestamp.valueOf( writeValueAccordingType("LocalDateTime", ldate) );
        statement.setTimestamp(index, param);
      }else if(fieldtype.equalsIgnoreCase("Timestamp")){
        Timestamp param=(Timestamp)obj;
        statement.setTimestamp(index, param);
      }else if(fieldtype.equalsIgnoreCase("Time")){
        Time param=(Time)obj;
        statement.setTime(index, param);
      }else if(fieldtype.equalsIgnoreCase("LocalTime")){
        LocalTime ldate=(LocalTime)obj;
        Time param=Time.valueOf( writeValueAccordingType("LocalTime", ldate) );
        statement.setTime(index, param);
      }else if(fieldtype.equalsIgnoreCase("boolean")){
        boolean param=(boolean)obj;
        statement.setBoolean(index, param);
      }else if(fieldtype.equalsIgnoreCase("byte[]")){
        byte[] param=(byte[])obj;
        statement.setBytes(index, param);
      }else{
        statement.setObject(index, obj);
      }
    }
    private void setStatement(PreparedStatement statement,Object[] objs)throws Exception{
      if(objs!=null){
        for(int i=0;i<objs.length;i++){
          setPreparedStatement(statement,objs[i],i+1);
        }
      }
    }
    private void setPreparedStatement(PreparedStatement statement,Field field,T objT,int index)throws Exception{
      field.setAccessible(true);
      setPreparedStatement( statement,field.get(objT),index);
      field.setAccessible(false);
    }
    public void setStatement(PreparedStatement statement,T objT,String[] fieldcondition)throws Exception{
      if(fieldcondition!=null){
        if(fieldcondition.length>0){
          Class<?> clazz=objT.getClass();
          Field ftemp=null;
          for(int i=0;i<fieldcondition.length;i++){
            ftemp=clazz.getDeclaredField(fieldcondition[i]); 
            setPreparedStatement(statement,ftemp,objT,i+1);
          }
        }
      }
    }

  public String[][] getValueString2dims(Connection connection,String sqlprepare,Object[] valuecondition) throws Exception {
      // Prépare la requête SQL avec des paramètres
      String sql = sqlprepare;
      // Crée une instruction préparée
      try (PreparedStatement statement = connection.prepareStatement(sql)) {
          setStatement(statement, valuecondition); 
          try (ResultSet resultSet = statement.executeQuery()) {
              Vector<String[]> vresultat=new Vector<String[]>();
              ResultSetMetaData rsmd=resultSet.getMetaData();
              int nbcolumn=rsmd.getColumnCount(); //nb colonne 
              boolean encore=resultSet.next(); //mbola misy ve le ligne
              String[] col=null;
              while(encore){//if==true
                  col=new String[nbcolumn];
                  for(int i=1;i<=nbcolumn;i++){
                    col[i-1]=resultSet.getString(i); //l'element en String du colonne numero i dans ce ligne
                  }
                  vresultat.add(col);
                  encore=resultSet.next();//resultSet.next() : ligne suivante / encore= false si plus de ligne
              }
              if(col==null){ return null; }
              String[][] resultat=new String[vresultat.size()][nbcolumn];
              for(int i=0;i<vresultat.size();i++){
                resultat[i]=vresultat.elementAt(i);
              }
              return resultat;
          }
      }
    }
    private List<HashMap<String,String>> valueSelectToListHashMap(String sqlSelect,Connection connection)throws Exception{
      List<HashMap<String,String>> lstdata=new ArrayList<HashMap<String,String>>();
      HashMap<String,String> data=null;
      createStatementIfNull(connection);
      ResultSet resultSet=statement.executeQuery(sqlSelect);//
      ResultSetMetaData rsmd=resultSet.getMetaData();
      int nbcolumn=rsmd.getColumnCount(); //nb colonne 
      boolean encore=resultSet.next(); //mbola misy ve le ligne
      while(encore){//if==true
          data=new HashMap<String,String>();
          for(int i=1;i<=nbcolumn;i++){
            data.put(rsmd.getColumnLabel(i), resultSet.getString(i)); //key:nom du colonne / value : valeur du colonne
            ////System.out.println("nom: "+rsmd.getColumnLabel(i)+"---->"+resultSet.getString(i));
          }
          lstdata.add(data);
          encore=resultSet.next();
      }
      resultSet.close();
      return lstdata;
    }
    public List<HashMap<String,String>> valueSelectToListHashMap(String sqlprepare,Object[] valuecondition,Connection connection)throws Exception{
      try (PreparedStatement statement = connection.prepareStatement(sqlprepare)) {
        setStatement(statement, valuecondition); 
        try (ResultSet resultSet = statement.executeQuery()) {
          List<HashMap<String,String>> lstdata=new ArrayList<HashMap<String,String>>();
          HashMap<String,String> data=null;
          ResultSetMetaData rsmd=resultSet.getMetaData();
          int nbcolumn=rsmd.getColumnCount(); //nb colonne 
          boolean encore=resultSet.next(); //mbola misy ve le ligne
          while(encore){//if==true
              data=new HashMap<String,String>();
              for(int i=1;i<=nbcolumn;i++){
                data.put(rsmd.getColumnLabel(i), resultSet.getString(i)); //key:nom du colonne / value : valeur du colonne
                ////System.out.println("nom: "+rsmd.getColumnLabel(i)+"---->"+resultSet.getString(i));
              }
              lstdata.add(data);
              encore=resultSet.next();
          }
          resultSet.close();
          return lstdata;
        }
      }
    }
//-----------------
public static byte[] convertFileToByteArray(File file) throws IOException {
  return Files.readAllBytes(file.toPath());
}
//------------------------------------------------------------------------------------------------------
public Object valuOfString(String type, String value){  
  Object object=value;
  if(type.compareTo("int")==0){ 
        object=Integer.valueOf(value);
  }else if(type.compareTo("long")==0){
        object=Long.valueOf(value);
  }else if(type.compareTo("float")==0){
        object=Float.valueOf((value));
  }else if(type.compareTo("char")==0){
        String str=value;
        object=str.charAt(0);
  }else if(type.compareTo("double")==0){
        object=Double.valueOf(value);
  }else if(type.compareTo("LocalDate")==0){ //annee-mois-jours
        String [] strDt=value.split("-");
        String[] forJour=strDt[2].split(" ");
        LocalDate date=LocalDate.of(Integer.valueOf(strDt[0]), Integer.valueOf(strDt[1]) , Integer.valueOf(forJour[0]) );
        
        object=date;
  }else if(type.compareTo("Date")==0){
        Date date=Date.valueOf(value);
        object=date;
  }else if(type.compareTo("LocalDateTime")==0){ //annee-mois-jours
      String[] ld1=value.split(" ");
      String[] ld2=ld1[0].split("-");
      String[] ld3=ld1[1].split(":");
      object=LocalDateTime.of(Integer.valueOf(ld2[0]),Integer.valueOf(ld2[1]),Integer.valueOf(ld2[2]),Integer.valueOf(ld3[0]),Integer.valueOf(ld3[1]),Integer.valueOf(ld3[2]));
  }else if (type.compareTo("Timestamp")==0){
    object=Timestamp.valueOf(value);
  }
  else if(type.compareTo("Time")==0){
        Time time=Time.valueOf(value);
        object=time;
  }else if(type.compareTo("LocalTime")==0){ 
        String[] temps=value.split(":");
        object= LocalTime.of(Integer.valueOf(temps[0]), Integer.valueOf(temps[1]),Integer.valueOf(temps[2]));
  }else if(type.compareTo("boolean")==0){
        object=Boolean.valueOf(value);
  }else if(type.compareTo("byte[]")==0){
    //System.out.println(value);
    object=Base64.getDecoder().decode(value);
}
  return object;
}
//------
public Object creerObject(Object objexample,HashMap<String,String> hashMapData)throws Exception{
  Object obj =objexample.getClass().getDeclaredConstructor().newInstance();
  Field[] fields=objexample.getClass().getDeclaredFields();
  Method method=null;
  String stemp=null;
  Object paramtemp=null;
  String strtemp="";
  for(int i=0;i<fields.length;i++){
    strtemp=getInfoDbName(fields[i]);
    if(strtemp==null){ strtemp=""; }
    stemp=hashMapData.get(strtemp); //alaina @ le InfoDbName , raha tsy annoter de le nom an'le field no par defaut
    if(stemp!=null){ //seul les noms de fields dans le cle valeur qu'on set
      if(stemp.compareTo("")!=0){
        paramtemp=valuOfString(fields[i].getType().getSimpleName(),stemp);
        fields[i].setAccessible(true);
        fields[i].set( obj , paramtemp );  //set field of obj
        fields[i].setAccessible(false);
      }
    }
  }
  return obj;    
}

//---------------------------------------------------creation du tableau d'objet dont leurs valeurs est dans un String[][]
public Object[] creerLstObjects(Object objexample,List<HashMap<String,String>> lstValueField)throws Exception{
    if(lstValueField==null){ return null; }
    Object[] lstobj=new Object[lstValueField.size()];
    for (int i=0;i<lstValueField.size();i++){ lstobj[i]=creerObject(objexample, lstValueField.get(i));  }
    return lstobj;
}
public T[] creerLstObjectsT(T tobj,List<HashMap<String,String>> lstValueField)throws Exception{
    if(lstValueField==null){ return null; }
    else if(lstValueField.isEmpty()){ return null; }
    T[] lstobj=(T[]) Array.newInstance(tobj.getClass(), lstValueField.size());
    for (int i=0;i<lstValueField.size();i++){ lstobj[i]=(T)creerObject(tobj, lstValueField.get(i));  }
    return lstobj;
}
//----------------------------------------------------
public Object[] creerLstObjects(Object objexample,Connection connection,String sqlSelect)throws Exception{
  List<HashMap<String,String>> lstValueField=valueSelectToListHashMap(sqlSelect,connection);
  Object[] lstobj=creerLstObjects(objexample, lstValueField);
  return lstobj;
}
public Object[] creerLstObjects(Object objexample,Connection connection,String sqlprepare,Object[] valuecondition)throws Exception{
  List<HashMap<String,String>> lstValueField=valueSelectToListHashMap(sqlprepare, valuecondition, connection);
  Object[] lstobj=creerLstObjects(objexample, lstValueField);
  return lstobj;
}
public T[] creerLstObjectsT(T tobj,Connection connection,String sqlSelect)throws Exception{
  List<HashMap<String,String>> lstValueField=valueSelectToListHashMap(sqlSelect,connection);
  T[] lstobj=creerLstObjectsT(tobj, lstValueField);
  return lstobj;
}
public T[] creerLstObjectsT(T tobj,Connection connection,String sqlprepare,Object[] valuecondition)throws Exception{
  List<HashMap<String,String>> lstValueField=valueSelectToListHashMap(sqlprepare, valuecondition, connection);
  T[] lstobj=creerLstObjectsT(tobj, lstValueField);
  return lstobj;
}
//-----------------------------------------------------

    public String writeValueAccordingType(String type,Object value){ //---date--2022/02/03---> '2022/02/03' , int--3--> 3
      if(type.compareTo("byte[]")==0){ return "\'"+Base64.getEncoder().encodeToString((byte[])value)+"\'"; }

      if(value==null){  return "NULL";  }
      int siString=type.compareTo("String");
      int siDate=type.compareTo("Date");
      int siLocalDate=type.compareTo("LocalDate");
      int siTime=type.compareTo("Time");
      int siLocalDateTime=type.compareTo("LocalDateTime");
      int sitimestamp=type.compareTo("Timestamp");

      int valeurfinal=siString*siDate*siLocalDate*siTime*siLocalDateTime; //---> si 0 --> v-d-r que 1 de ces type est vrai
      if(siLocalDateTime==0 ){
         LocalDateTime ldatetime = (LocalDateTime)value;
         DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
         String formattedDateTime = "\'"+ldatetime.format(formatter)+"\'";
          return formattedDateTime;
      }if(sitimestamp==0){
        Timestamp timestamp=(Timestamp)value;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime ="\'"+timestamp.toLocalDateTime().format(formatter)+"\'";
        return formattedDateTime;
      }
      else if(siDate==0){
          SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
          Date date=(Date)value;
          return "\'"+simpleDateFormat.format(date).toString()+"\'";
      }else if(siLocalDate==0){
          SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
          LocalDate ldate=(LocalDate)value;
          return "\'"+simpleDateFormat.format(Date.valueOf(ldate)).toString()+"\'";
      }else if(valeurfinal==0){
        String val="\'"+value+"\'";
        return val;
      }else{ return value.toString()+""; }
    }
//------------------------------------------------------exemple= "nom"----to---->"getNom"  et   "nom"----to---->"setNom" 
      public String to_getAttribu(String nomAttribu,String typeField)
      {
            String getAttribMaj="get";
            if(typeField.compareToIgnoreCase("boolean")==0){  getAttribMaj="is";  }
            String attrib=nomAttribu.substring(0,1).toUpperCase(); //rendre en majuscul la premiere lettre du nomAtribu
            String restattrib=nomAttribu.substring(1,nomAttribu.length());  //prendre les lettres a partir du 2e lettre
            getAttribMaj=getAttribMaj.concat(attrib+restattrib);  //fusionner pour avoir le nom de fonction "getAtribu"
                         //Method m=c.getMethod("getVar1");
            return getAttribMaj;
      }
      public String to_setAttribu(String nomAttribu)
      {
            String getAttribMaj="set";
            String attrib=nomAttribu.substring(0,1).toUpperCase(); //rendre en majuscul la premiere lettre du nomAtribu
            String restattrib=nomAttribu.substring(1,nomAttribu.length());  //prendre les lettres a partir du 2e lettre=
            getAttribMaj=getAttribMaj.concat(attrib+restattrib); //fusionner pour avoir le nom de fonction "getAtribu"
             //Method m=c.getMethod("getVar1");
            return getAttribMaj;
      }
//---------------------------------------------------------------------------------------------------------------------save/Insert
  private void save(Object object,Connection connection,String nameprimaryKey)throws Exception{
    String siPkNull="##$$##$$;;";
    if(nameprimaryKey==null){ nameprimaryKey=siPkNull; }
    String nom=getInfoDbName(object);
    String requet="insert into "+nom + " (";
    Field[] fields=object.getClass().getDeclaredFields();
    String values=" values(";
    String typepk="";
    HashMap<String,String> hashMap=columnsInDataBase(nom, connection.getMetaData());
    String column=null;
    List<Object> listObj=new ArrayList<>();
    for(int i=0;i<fields.length;i++){
      column=hashMap.get(getInfoDbName(fields[i]));
      if(column!=null ){ //si il n'est pas le pk et qu'il existe comme column dans la table qui lui convient
        if(column.compareToIgnoreCase(nameprimaryKey)!=0){
          Object obj= object.getClass().getMethod( to_getAttribu(fields[i].getName(),fields[i].getType().toString()) ).invoke(object) ;
          if(obj!=null){ 
            requet=requet+column;
            listObj.add(obj);
            values=values+" ? "; 
            requet=requet+",";  values=values+",";
          }
        }else if(column.compareToIgnoreCase(nameprimaryKey)==0){ //si c'est le pk et qu'il existe dans la table du bdd qui lui convient
          typepk=fields[i].getType().getSimpleName();
        }
      }
    }
    requet=requet.substring(0, requet.length()-1);
    values=values.substring(0, values.length()-1);
    requet=requet+")"+values+")";
    //System.out.println("before execut : "+requet); 
    this.executeNoSelect(connection, requet, listObj.toArray());
    //System.out.println("after execut : "+requet); 
    if(nameprimaryKey.compareToIgnoreCase(siPkNull)!=0){
      String[][] lastId=this.valueSelectToString("select max("+nameprimaryKey+") from "+object.getClass().getSimpleName(), connection);
      Method[] sesMeths=object.getClass().getDeclaredMethods();
      int u=-1;
      for(int i=0;i<sesMeths.length;i++){   if(sesMeths[i].getName().compareTo( to_setAttribu(nameprimaryKey))==0 &&  sesMeths[i].getParameterTypes()[0].getSimpleName().compareTo(typepk)==0){ u=i;}   }
      if(u>=0 && u<sesMeths.length){   sesMeths[u].invoke(object,Integer.valueOf(lastId[0][0])); }
    }
  }

  public void save(Object object,Connection connection)throws Exception{
    save(object, connection,getInfoDbNameFieldAnnoteByAnnotation((T)object,Id.class));
  }
//----------------------------------------------------------------------------------------------------------update
  private void update(Object object,Connection connection,String nameprimaryKey)throws Exception{ 
    //update emp set col=valCol where condition
    String nom=getInfoDbName(object); 
    String requete="update "+nom + " set ";
    String colAndVal="";
    Field[] fields=object.getClass().getDeclaredFields();
    HashMap<String,String> hashMap=columnsInDataBase(nom, connection.getMetaData());
    String column=null;
    List<Object> listobjs=new ArrayList<>();
    for(int i=0;i<fields.length;i++){
      column=hashMap.get(getInfoDbName(fields[i]));
      if(column!=null){//si l'attribu est un du column de la table de convenance
        Object val=object.getClass().getMethod( to_getAttribu(fields[i].getName(), fields[i].getType().getSimpleName() ) ).invoke( object );
        if(val!=null){ 
          listobjs.add(val);
          colAndVal=colAndVal+" "+column+"= ? ,"; 
        }
      }
    }
    if(colAndVal.length()>0){ colAndVal=colAndVal.substring(0, colAndVal.length()-1); } //on prend en enlevant la virgule a la fin 
    if(nameprimaryKey!=null){
      Field fpk=null;
      try{fpk=object.getClass().getDeclaredField(nameprimaryKey);}
      catch(Exception e){throw new Exception("fields "+nameprimaryKey+" inexistant dans class"+nom);}
      Object valpk=object.getClass().getMethod( to_getAttribu( nameprimaryKey, fpk.getType().getSimpleName() ) ).invoke(object); //le pk de this pour update 
      colAndVal=colAndVal+" where "+nameprimaryKey+"= ? ";
      listobjs.add(valpk);
    }else{ throw new Exception("pk null, nom primary key obligatoire pour update"); }
    requete=requete+colAndVal;
    ////System.out.println("before execut : "+requete); 
    this.executeNoSelect(connection, requete, listobjs.toArray());
    ////System.out.println("after execut : "+requete); 
  }

  public void update(Object object,Connection connection)throws Exception{
    update(object,connection,getInfoDbNameFieldAnnoteByAnnotation((T)object, Id.class));
  }
//-------------------------------------------------------------------------------------------------------------delete
  private void delete(Object object,Connection connection,String nameprimaryKey)throws Exception{
    String nom=getInfoDbName(object); 
    String requet="delete from "+nom;
    String condition=" where";
    List<Object> listobjs=new ArrayList<>();
    if(nameprimaryKey==null){ //raha tsy par cle primaire no hi-deleteny azy
      Field[] fields=object.getClass().getDeclaredFields();
      HashMap<String,String> hashMap=columnsInDataBase(nom, connection.getMetaData());
      String column=null;
      for(int i=0;i<fields.length;i++){
        column=hashMap.get(getInfoDbName(fields[i]));
        if(column!=null){
          condition=condition+" ";
          Object val=object.getClass().getMethod( to_getAttribu(fields[i].getName(), fields[i].getType().getSimpleName()) ).invoke( object );
          if(val!=null){ 
            listobjs.add(val);
            condition=condition+" "+column+"= ? and"; 
          }
        }
      }
      if(condition.length()>0){ condition=condition.substring(0, condition.length()-3); } //on prend en enlevant la virgule a la fin
    }else{
      Field fpk=object.getClass().getDeclaredField(nameprimaryKey);
      Object valpk=object.getClass().getMethod( to_getAttribu( nameprimaryKey, fpk.getType().getSimpleName() ) ).invoke(object); 
      listobjs.add(valpk);
      condition=condition+" "+nameprimaryKey +"= ? ";
    }
    requet=requet+condition;
    ////System.out.println("before execut : "+requet);
    this.executeNoSelect(connection, requet, listobjs.toArray());
    //////System.out.println("after execut : "+requet); 
  }

  public void delete(Object object,Connection connection)throws Exception{
    delete(object,connection,getInfoDbNameFieldAnnoteByAnnotation((T)object,Id.class));
  }
//---------------------------------------------------------------------------------------------------------------
  public Object find(Object object,Connection connection,String nameprimaryKey)throws Exception{
    String nom=getInfoDbName(object); 
    String requet="select * from "+nom;
    String condition=" where ";
    String column="";
    List<Object> listobjs=new ArrayList<>();
    if(nameprimaryKey==null){
      Field[] fields=object.getClass().getDeclaredFields();
      HashMap<String,String> hashMap=columnsInDataBase(nom, connection.getMetaData());
      for(int i=0;i<fields.length;i++){
        column=hashMap.get(getInfoDbName(fields[i]));
        if(column!=null){
          condition=condition+" ";
          Object val=object.getClass().getMethod( to_getAttribu(fields[i].getName(),fields[i].getType().getSimpleName()) ).invoke( object );
          if(val!=null){ 
            listobjs.add(val);
            condition=condition+" "+column+"= ? and";
          }
        }
      }
      if(condition.length()>0){ condition=condition.substring(0, condition.length()-3); } //on prend en enlevant la virgule a la fin
    }else{
      Field fpk=object.getClass().getDeclaredField(nameprimaryKey);
      Object valpk=object.getClass().getMethod( to_getAttribu( nameprimaryKey, fpk.getType().getSimpleName() ) ).invoke(object); 
      listobjs.add(valpk);
      condition=condition+" "+nameprimaryKey +"= ? ";
    }
    requet=requet+condition;
    ////////////
    ////System.out.println("before execut : "+requet);
    Object[] objects=this.creerLstObjects(object, connection, requet, listobjs.toArray());
    ////System.out.println("after execut : "+requet); 
    ///////////
    if(objects==null){ return null; }
    return objects[0];
  }

  private T findT(T object,Connection connection,String nameprimaryKey)throws Exception{
    String nom=getInfoDbName(object); 
    String requet="select * from "+nom;
    String condition=" where ";
    String column="";
    List<Object> listobjs=new ArrayList<>();
    if(nameprimaryKey==null){
      Field[] fields=object.getClass().getDeclaredFields();
      HashMap<String,String> hashMap=columnsInDataBase(nom, connection.getMetaData());
      for(int i=0;i<fields.length;i++){
        column=hashMap.get(getInfoDbName(fields[i]));
        if(column!=null){
          condition=condition+" ";
          Object val=object.getClass().getMethod( to_getAttribu(fields[i].getName(),fields[i].getType().getSimpleName()) ).invoke( object );
          if(val!=null){
            listobjs.add(val);
            condition=condition+" "+fields[i].getName()+"= ? and";
          }
        }
      }
      if(condition.length()>0){ condition=condition.substring(0, condition.length()-3); } //enlevant le "and" a la fin
    }else{
      Field fpk=object.getClass().getDeclaredField(nameprimaryKey);
      Object valpk=object.getClass().getMethod( to_getAttribu( nameprimaryKey, fpk.getType().getSimpleName() ) ).invoke(object); 
      listobjs.add(valpk);
      condition=condition+" "+nameprimaryKey +"= ? ";
    }
    requet=requet+condition;
    //System.out.println("before execut : "+requet);
    T[] objs=this.creerLstObjectsT(object, connection,requet, listobjs.toArray());
    //System.out.println("after execut : "+requet); 
    if(objs==null){ return null; }

    return objs[0];
  }
  public T findT(T tobj,Connection connection)throws Exception{
    T treponse=findT(tobj, connection, getInfoDbNameFieldAnnoteByAnnotation(tobj,Id.class));
    return treponse;
  }
  
//---------------------------------------------------------------------------------------------------------------find All 
  public Object[] findAll(Object object,Connection connection)throws Exception{
    Object[] objs=this.creerLstObjects(object,connection, "select * from "+object.getClass().getSimpleName());
    return objs;
  }
  public T[] findAllT(T object,Connection connection)throws Exception{
    T[] objs=this.creerLstObjectsT(object,connection, "select * from "+object.getClass().getSimpleName());
    return objs;
  }

  
}

