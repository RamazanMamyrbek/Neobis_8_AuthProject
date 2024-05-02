FROM openjdk:21-oracle
COPY target/*.jar auth-backend.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "auth-backend.jar"]
