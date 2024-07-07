package gno;
import java.lang.annotation.*;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME) //RUNTIME no ampiasatsika aloha zao
@Target ( ElementType.FIELD) //manisy virgule , Element.METHOD
public @interface Id {
}
