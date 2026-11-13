# ---------- Stage 1: build with Maven on JDK 17 ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q -DskipTests package

# ---------- Stage 2: small JRE runtime image ----------
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN groupadd -r taskflow && useradd -r -g taskflow taskflow
COPY --from=build /app/target/taskflow-1.0.0.jar app.jar
USER taskflow
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
