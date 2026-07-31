# Etapa 1: compilar el proyecto con Maven (imagen solo usada para compilar,
# no queda en la imagen final)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests clean package

# Etapa 2: imagen final, liviana, con las librerias de fuentes (freetype/fontconfig)
# que JasperReports necesita para poder generar PDFs correctamente.
FROM eclipse-temurin:21-jre
RUN apt-get update \
    && apt-get install -y --no-install-recommends fontconfig libfreetype6 fonts-dejavu-core \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=build /app/target/proyecto-red.jar app.jar

CMD ["java", "-jar", "app.jar"]
