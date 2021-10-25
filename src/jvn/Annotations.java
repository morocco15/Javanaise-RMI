package jvn;
import java.lang.annotation.*;

// The annotation is available at execution time
@Retention(RetentionPolicy.RUNTIME)

@Target(ElementType.METHOD)
public @interface Annotations {
    String type();
}