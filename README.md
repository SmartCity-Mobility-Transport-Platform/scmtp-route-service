## SCMTP Route Service (Java)

GraphQL service for managing routes, stops, and schedules in the SCMTP system, implemented in **Java + Spring Boot**.

### Stack

- **Language**: Java 17
- **Framework**: Spring Boot (Web, Data JPA, GraphQL)
- **Database**: PostgreSQL (DB-per-service)
- **GraphQL endpoint**: `/graphql`
- **Health endpoint**: `/actuator/health`

### GraphQL Schema (contract)

Matches the architecture spec:

- **Types**
  - **Route**: `id`, `name`, `stops`, `schedules`
  - **Stop**: `id`, `name`, `latitude`, `longitude`
  - **Schedule**: `id`, `routeId`, `departureTime`, `arrivalTime`, `daysOfWeek`
- **Queries**
  - **routes**: list all routes with stops & schedules
  - **route(id: ID!)**: get a single route
  - **stopsByLocation(lat, lng, radiusKm)**: find nearby stops
  - **nextDepartures(stopId, fromTime)**: upcoming departures for a stop

Defined in `src/main/resources/graphql/schema.graphqls` and implemented via Spring GraphQL controllers.

### Integration with other services

- **API Gateway / scmtp-infra**
  - Ingress or API gateway can route:
    - `/api/routes/graphql` → `scmtp-route-service:4000/graphql`
  - Service is HTTP-only, no DB or code sharing between services.
- **Kubernetes / probes**
  - Liveness/readiness via Spring Actuator:
    - `/actuator/health`
  - Configure K8s probes against this endpoint.
- **Other microservices**
  - Ticketing, payment, wallet, user, notification services call this service via **GraphQL over HTTP** through the gateway, same contract regardless of language.

### Configuration

All configuration is via environment variables, making it easy to integrate in separate infra repos:

- **Port**

```bash
SERVER_PORT=4000
```

- **Database (PostgreSQL)**

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/scmtp_route
SPRING_DATASOURCE_USERNAME=scmtp
SPRING_DATASOURCE_PASSWORD=scmtp
```

Defaults are present in `application.yml` but you should override them via env vars in production/K8s.

### Build & Run (local)

From the project root:

```bash
mvn clean package
java -jar target/route-service-0.1.0-SNAPSHOT.jar
```

Then:

- GraphQL: `http://localhost:4000/graphql`
- GraphiQL UI (if enabled): same `/graphql` endpoint
- Health: `http://localhost:4000/actuator/health`

### Docker

Build image:

```bash
docker build -t scmtp-route-service-java .
```

Run container:

```bash
docker run -p 4000:4000 \
  -e SERVER_PORT=4000 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/scmtp_route \
  -e SPRING_DATASOURCE_USERNAME=scmtp \
  -e SPRING_DATASOURCE_PASSWORD=scmtp \
  scmtp-route-service-java
```

The resulting image can be pushed to DockerHub/ECR and referenced from your `scmtp-infra` deployment manifests.



