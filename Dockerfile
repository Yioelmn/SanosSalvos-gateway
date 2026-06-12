# Etapa 1: Compilar la aplicación Java usando Maven
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copiar el archivo de configuración de Maven y dependencias primero (aprovecha la caché de Docker)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar el código fuente y compilar el archivo .jar saltándose los tests para ahorrar tiempo en Render
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Imagen de ejecución (JDK ligero)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copiar el archivo .jar generado desde la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Exponer el puerto del Gateway
EXPOSE 8080

# Configurar variables de optimización de memoria para contenedores ligeros
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Comando para ejecutar el Gateway
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]