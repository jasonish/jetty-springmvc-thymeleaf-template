Template - Embedded Jetty/SpringMVC/Thymeleaf Application
=========================================================

## Summary

This is a standalone Spring based Java application that embeds Jetty
and uses SpringMVC as its web tier.

It is a template that demonstrates embedding Jetty and SpringMVC into
your application as a web tier instead of deploying your application
into a container.

Thymeleaf is used as a view technology greatly simplifying the process
compared to using JSP views with embedded Jetty.

## Running

### With Maven

    mvn exec:java -Dexec.mainClass=ca.unx.template.Main

Then point your browser at http://localhost:8080.

### Basic Jar

The default package goal will build a jar in the target directory with
dependency jars copied into target/lib.

    mvn package
    java -jar target/jetty-springmvc-thymeleaf-template-0.0.1-SNAPSHOT.jar

Then point your browser at http://localhost:8080.

### Fat Jar

If you would rather have a single jar that contains all the
dependencies, use the 'fatjar' profile.

    mvn -Pfatjar package
    java -jar target/jetty-springmvc-thymeleaf-template-0.0.1-SNAPSHOT.jar

Then point your browser at http://localhost:8080.

## Other Notes

- To change the port that jetty will start on, set the jetty.port property
  to the desired port.
  
