FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn -q -e -B dependency:go-offline

COPY src ./src
RUN mvn -q -e -B package -DskipTests

FROM eclipse-temurin:17-jre-alpine AS runtime

WORKDIR /app

ENV JAVA_OPTS=""
ENV SERVER_PORT=4000

COPY --from=build /app/target/route-service-0.1.0-SNAPSHOT.jar app.jar

EXPOSE 4000

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]



