# TaskFlow - Team Task Manager

A complete full-stack Java application by **Abel Haile Magicho**:
a Spring Boot REST API, a single-page web frontend served by the same
app, container packaging, Kubernetes manifests, and deployment guides
for Azure and AWS. One codebase, one deployable artifact, every stage
of the journey from a laptop to the cloud.

## Architecture

```
 Browser
    |
    |  HTML, CSS, JavaScript single-page app
    v
 Spring Boot 3 on Java 17  -------  /api/tasks REST API
    |                                   |
    |  Spring Data JPA                  |  Bean Validation
    v                                   v
 H2 in memory            or        PostgreSQL
 local profile                     postgres profile
```

- **Backend:** Java 17, Spring Boot 3.3, Spring Web, Spring Data JPA,
  Bean Validation, Maven.
- **Frontend:** one static page with vanilla HTML, CSS and JavaScript in
  `src/main/resources/static`. The Spring app serves it, so the whole
  product ships as a single jar or a single container image.
- **Database:** H2 in memory for local development with zero setup.
  PostgreSQL when the `postgres` Spring profile is active, which is how
  Docker, Kubernetes, Azure and AWS run it.

## The API

| Method | Path                    | What it does                    |
|--------|-------------------------|---------------------------------|
| GET    | /api/tasks              | List all tasks                  |
| GET    | /api/tasks?status=DONE  | List tasks filtered by status   |
| GET    | /api/tasks/{id}         | Get one task                    |
| POST   | /api/tasks              | Create a task                   |
| PUT    | /api/tasks/{id}         | Replace a task                  |
| PATCH  | /api/tasks/{id}/complete| Mark a task DONE                |
| DELETE | /api/tasks/{id}         | Delete a task                   |
| GET    | /api/tasks/stats        | Counts: total, todo, in progress, done |

A task has: `id`, `title`, `description`, `status` (TODO, IN_PROGRESS,
DONE), `priority` (LOW, MEDIUM, HIGH), `dueDate`, `createdAt`.

## Run it locally

Needs JDK 17 and Maven 3.9.

```bash
mvn spring-boot:run
# or
mvn package
java -jar target/taskflow-1.0.0.jar
```

Open http://localhost:8080 for the app. The API answers at
http://localhost:8080/api/tasks. Data lives in the H2 in-memory
database and resets on restart.

Run the tests with:

```bash
mvn test
```

## Run it with Docker

App plus PostgreSQL, wired together with Docker Compose:

```bash
docker compose up --build
```

Open http://localhost:8080. Stop and clean up with
`docker compose down -v`.

Plain Docker, app only with the local H2 profile:

```bash
docker build -t taskflow:1.0.0 .
docker run -p 8080:8080 taskflow:1.0.0
```

## Run it on Kubernetes

Manifests live in `k8s`: ConfigMap, Secret example, Deployment and
Service for the app and the database, plus an Ingress.

```bash
kubectl apply -f k8s/
kubectl rollout status deployment/taskflow
```

Set the image in `k8s/deployment.yaml` to your registry image first,
and replace the example Secret value with a real database password.

## Deploy to Azure

Full guide: [azure-deploy.md](azure-deploy.md). In short: build the
image into Azure Container Registry with `az acr build`, create an AKS
cluster, then `kubectl apply -f k8s/`.

## Deploy to AWS

Full guide: [aws-deploy.md](aws-deploy.md) with the ECS task definition
in [aws/ecs-task-definition.json](aws/ecs-task-definition.json). In
short: push the image to ECR, register the task definition, run it as
an ECS Fargate service. The same `k8s` manifests also deploy to EKS
unchanged.

## CI/CD

`.github/workflows/ci-cd.yml` runs on every push to `main`: build and
test with Maven, build the Docker image, then deploy stages for Azure
AKS and for AWS EKS and ECS. Cloud credentials are read from GitHub
secrets; see the two deploy guides for the secret names.

## Project layout

```
src/main/java/com/abel/taskflow
  TaskflowApplication.java     entry point
  model/Task.java              JPA entity with validation
  model/TaskStatus.java        TODO, IN_PROGRESS, DONE
  model/Priority.java          LOW, MEDIUM, HIGH
  repository/TaskRepository.java
  controller/TaskController.java
src/main/resources/static/index.html   the whole frontend
Dockerfile                     multi-stage build
docker-compose.yml             app plus PostgreSQL
k8s/                           Kubernetes manifests
aws/                           ECS task definition
.github/workflows/ci-cd.yml    pipeline
```

## Author

Abel Haile Magicho - GitHub: https://github.com/kembAB
