package main;
import java.io.File;
import java.lang.annotation.Annotation;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Time;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Vector;
import java.lang.reflect.*;

import javax.lang.model.element.Name;
import javax.swing.text.View;

import connect.*;
import entite.*;
import gno.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.Base64;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.io.FileWriter;
import java.io.BufferedWriter;
public class Affiche {
    public static void main(String[] args)throws Exception{
        Connect connect=new Connect();
        Connection connection=connect.getConnectionPsql();
        Test4 test4=new Test4();
        // test4=new Test4(10, Date.valueOf("2024-04-29"), Timestamp.valueOf("2024-04-29 20:22:11"), "prepare", 35, Time.valueOf("11:00:09"));
        // test4.create(connection);
        test4=new Test4(10, Date.valueOf("2024-04-29"), Timestamp.valueOf("2024-12-21 21:00:11"), "koko", 21, Time.valueOf("11:00:09"));
        // test4.update(connection);
        // test4=test4.readById(connection);
        // test4.systemoutfield();
        // test4.setId(1);
        // test4.delete(connection);
        //Test4[] test4s=test4.read(connection);
        Test4[] test4s=test4.readByQueryConvenable(connection,"select * from test4 where nom= ?",new Object[]{"koko\' or \'6\'!=\'5",Timestamp.valueOf("2024-12-21 21:00:11.0")});
        //select * from test4 where nom='koko' or '6'!='5' and datyh='2024-12-21 21:00:11.0'
        //test4s=test4.readById(connection);
        //select * from test4 where nom='koko' ? and datyh=?
        for(int i=0;i<test4s.length;i++){
            test4s[i].systemoutfield();
        }
        connection.close();
        // Etudiant etudiant=new Etudiant();
        // etudiant.setIdetudiant(5);
        // System.out.println(etudiant.readOneByQueryConvenable(connection, "select * from etudiant where nom=\'Li\'").getNom());

        // Etudiant[] etudiants2=etudiant.readByQueryConvenable(connection, "select * from etudiant");
        // for(int i=0;i<etudiants2.length;i++){
        //     System.out.println(etudiants2[i].getIdetudiant()+" "+etudiants2[i].getNumero()+"  "+etudiants2[i].getNom()+" "+etudiants2[i].getPrenoms()+" "+etudiants2[i].getNees()+" "+etudiants2[i].getAge());
        // }
        // Etudiant etudiant2=new Etudiant(11, 989, "JMama", "PPoopub", Date.valueOf("1993-09-08"), 19, Date.valueOf("2024-02-01"));
        // //etudiant2.create(connection);
        // //etudiant2.delete(connection);
        // etudiant2.update(connection);





    }

}

