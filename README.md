# Employee Management REST API

A small, layered Spring Boot API for the Codenzic Innovations Java assessment. It uses Java 21, Spring Boot 3.5, Maven, PostgreSQL, Bean Validation, Spring Data JPA, and OpenAPI.

## Requirements

- JDK 21 (verify with `java -version`)
- Maven 3.6.3 or newer (verify with `mvn -version`)
- PostgreSQL 14 or newer, or Docker Desktop with Compose
- IntelliJ IDEA or Visual Studio Code with Java support (optional)

## Quick start with Docker PostgreSQL

1. Clone the repository and enter its root folder:

   ```shell
   git clone <your-github-repository-url>
   cd employee-management-api
   ```

2. Start PostgreSQL. This creates a local database `employee_management` with user/password `postgres`/`postgres` on port `5432`:

   ```shell
   docker compose up -d
   ```

   Or create a PostgreSQL database manually and configure the environment variables below.

3. Build and run the tests:

   ```shell
   mvn clean verify
   ```

4. Start the API:

   ```shell
   mvn spring-boot:run
   ```

5. Open the API docs at <http://localhost:8080/swagger-ui.html>. OpenAPI JSON is at <http://localhost:8080/api-docs>.

The application listens on port `8080`. Hibernate creates/updates the `employees` table on startup. No seed data is inserted; create an employee with Swagger UI or Postman.

## Database configuration

The defaults match `docker-compose.yml`:

| Setting | Default |
| --- | --- |
| Host | `localhost` |
| Port | `5432` |
| Database | `employee_management` |
| Username | `postgres` |
| Password | `postgres` |

To use different values, copy `.env.example` to `.env` and set the equivalent environment variables in the shell or your IDE run configuration. Spring Boot reads operating-system environment variables; it does not automatically load `.env` files.

PowerShell example:

```powershell
$env:DB_HOST = "localhost"
$env:DB_PORT = "5432"
$env:DB_NAME = "employee_management"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "your-local-password"
$env:SERVER_PORT = "8080"
mvn spring-boot:run
```

Linux/macOS example:

```shell
export DB_HOST=localhost DB_PORT=5432 DB_NAME=employee_management
export DB_USERNAME=postgres DB_PASSWORD=your-local-password SERVER_PORT=8080
mvn spring-boot:run
```

Never commit real credentials. `.env` is ignored by Git.

## IDE setup

### IntelliJ IDEA

Choose **File → Open**, select the folder containing `pom.xml`, and import it as a Maven project. Set the project SDK and language level to Java 21. Start PostgreSQL, then run `EmployeeManagementApplication` (or use the Maven tool window to run `spring-boot:run`). Set database environment variables in **Run → Edit Configurations** if you changed the defaults.

### Visual Studio Code

Open the repository folder and install the **Extension Pack for Java**. Set `JAVA_HOME` to JDK 21, start PostgreSQL, then use the integrated terminal to run `mvn spring-boot:run`. The Java extension can also run `EmployeeManagementApplication` directly.

## API endpoints

Base path: `/api/v1/employees`

| Method | Path | Description | Success |
| --- | --- | --- | --- |
| `POST` | `/api/v1/employees` | Create an employee | `201 Created` |
| `GET` | `/api/v1/employees` | List employees, with pagination | `200 OK` |
| `GET` | `/api/v1/employees/{id}` | Get one employee | `200 OK` |
| `PUT` | `/api/v1/employees/{id}` | Replace employee details | `200 OK` |
| `DELETE` | `/api/v1/employees/{id}` | Delete an employee | `204 No Content` |

List query parameters:

- `page` (zero-based, default `0`), `size` (default `20`), `sort` (default `lastName`)
- `search` (matches first name, last name, email, or department)
- `status`: `ACTIVE`, `INACTIVE`, or `ON_LEAVE`

Example: `GET /api/v1/employees?search=engineering&status=ACTIVE&page=0&size=10&sort=lastName,asc`

### Create request

```json
{
  "firstName": "Asha",
  "lastName": "Rao",
  "email": "asha.rao@example.com",
  "department": "Engineering",
  "jobTitle": "Backend Developer",
  "salary": 75000.00,
  "status": "ACTIVE"
}
```

Create and update requests require all fields. Names, department, and job title must not be blank. Email must be valid and unique (case-insensitive). Salary must be greater than zero, with at most two decimal places. Status must be one of the documented enum values.

### Example response

```json
{
  "id": 1,
  "firstName": "Asha",
  "lastName": "Rao",
  "email": "asha.rao@example.com",
  "department": "Engineering",
  "jobTitle": "Backend Developer",
  "salary": 75000.00,
  "status": "ACTIVE",
  "createdAt": "2026-10-21T12:00:00Z",
  "updatedAt": "2026-10-21T12:00:00Z"
}
```

List responses use Spring's standard `Page` shape with `content`, `totalElements`, `totalPages`, `number`, `size`, and paging metadata.

## Error response

Errors share a stable shape. Validation errors include a `validationErrors` object keyed by field.

```json
{
  "timestamp": "2026-10-21T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Request validation failed",
  "path": "/api/v1/employees",
  "validationErrors": {
    "email": "must be a well-formed email address"
  }
}
```

HTTP status mapping: `400` invalid input, `404` employee not found, `409` duplicate email/data conflict, `500` unexpected server error.

## Postman

Import `postman/Employee Management API.postman_collection.json` into Postman. The collection uses `baseUrl` (`http://localhost:8080`) and automatically saves the created employee ID as `employeeId` for the follow-up requests.

## Project layout

```text
src/main/java/com/codenzic/employeemanagement/
  controller/   HTTP endpoints
  service/      business rules
  repository/   Spring Data persistence
  entity/       JPA model
  dto/          request and response contracts
  mapper/       entity/DTO mapping
  exception/    domain exceptions and global error handling
```

Tests cover service behavior and request validation. Run all checks with `mvn clean verify`.
