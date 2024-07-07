package connect;
import java.sql.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Connect
{ 
    
    public Connect(){
    }
    public Connection getConnectionPsql()throws Exception{
        Connection connection;
        //étape 1: charger la classe de driver
        Class.forName("org.postgresql.Driver");
        //étape 2: créer l'objet de connexion
        connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/test","postgres","motmirado");
        return connection;
    } 

} 