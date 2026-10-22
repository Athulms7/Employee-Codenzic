# Employee Management REST API

A small, layered Spring Boot API for the Codenzic Innovations Java assessment. It uses Java 25, Spring Boot 3.5, Maven, PostgreSQL, Bean Validation, Spring Data JPA, and OpenAPI.

## Requirements

- JDK 25 (verify with `java -version`)
- Maven 3.9 or newer (verify with `mvn -version`)
- PostgreSQL 14 or newer, or Docker Desktop with Compose
- IntelliJ IDEA or Visual Studio Code with Java support (optional)

## Quick start with Docker PostgreSQL

1. Clone the repository and enter its root folder:

   ```shell
   git clone <your-github-repository-url>
   cd employee-management-api
   ```

2. Start PostgreSQL. This creates a local database `employee_management` with user/password `postgres`/`postgres` on host port `5433` (PostgreSQL listens on `5432` inside the container):

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

Check that the application is responding with `GET http://localhost:8080/health`. A healthy response is `200 OK` with `{"status":"UP"}`.

## Database configuration

The defaults match `docker-compose.yml`:

| Setting | Default |
| --- | --- |
| Host | `localhost` |
| Port | `5433` |
| Database | `employee_management` |
| Username | `postgres` |
| Password | `postgres` |

The tracked configuration template is `src/main/resources/application.properties.example`. Before running the application, create the local properties file from this template:

PowerShell:

```powershell
Copy-Item src/main/resources/application.properties.example src/main/resources/application.properties
```

Linux/macOS:

```shell
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

`src/main/resources/application.properties` is ignored by Git so local settings and credentials are not committed. Edit that local file to configure the database, or override its environment-variable placeholders in the shell or IDE run configuration. Spring Boot reads operating-system environment variables; it does not automatically load `.env` files. `.env.example` is provided for reference, but copy its values into your shell or run configuration as needed.

PowerShell example:

```powershell
$env:DB_HOST = "localhost"
$env:DB_PORT = "5433"
$env:DB_NAME = "employee_management"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "your-local-password"
$env:SERVER_PORT = "8080"
mvn spring-boot:run
```

Linux/macOS example:

```shell
export DB_HOST=localhost DB_PORT=5433 DB_NAME=employee_management
export DB_USERNAME=postgres DB_PASSWORD=your-local-password SERVER_PORT=8080
mvn spring-boot:run
```

Never commit real credentials. `.env` is ignored by Git.

## IDE setup

### IntelliJ IDEA

Choose **File → Open**, select the folder containing `pom.xml`, and import it as a Maven project. Set the project SDK and language level to Java 25. Start PostgreSQL, then run `EmployeeManagementApplication` (or use the Maven tool window to run `spring-boot:run`). Set database environment variables in **Run → Edit Configurations** if you changed the defaults.

### Visual Studio Code

Open the repository folder and install the **Extension Pack for Java**. Set `JAVA_HOME` to JDK 25, start PostgreSQL, then use the integrated terminal to run `mvn spring-boot:run`. The Java extension can also run `EmployeeManagementApplication` directly.

## API endpoints

Base path: `/api/v1/employees`

| Method | Path | Description | Success |
| --- | --- | --- | --- |
| `GET` | `/health` | Check application health | `200 OK` |
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

Paging is zero-based. The default page size is `20`, and results are sorted by `lastName` ascending unless another sort is supplied. For example:

```shell
curl "http://localhost:8080/api/v1/employees?page=1&size=5&sort=lastName,desc"
```

The response includes `content`, `number` (current page), `size`, `totalElements`, and `totalPages`, along with Spring Data's other page metadata. Use `search` and `status` together with the paging parameters to narrow the results.

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

Successful response bodies are endpoint-specific; there is no common message/data envelope:

- `POST /api/v1/employees` returns `201 Created` and the created employee object.
- `GET /api/v1/employees` returns `200 OK` and a paginated Spring `Page` object with employees in `content`.
- `GET /api/v1/employees/{id}` and `PUT /api/v1/employees/{id}` return `200 OK` and an employee object.
- `DELETE /api/v1/employees/{id}` returns `204 No Content` with an empty body.
- `GET /health` returns `200 OK` with `{"status":"UP"}`.

Errors use a consistent shape; `validationErrors` identifies the invalid field or parameter and explains the expected value.

```json
{
  "timestamp": "2026-10-21T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Request validation failed",
  "path": "/api/v1/employees",
  "validationErrors": {
    "email": "must be a valid email address",
    "salary": "must be greater than 0"
  }
}
```

For an invalid enum in a request body, the response identifies the field and permitted values:

```json
{
  "timestamp": "2026-10-21T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Request body contains an invalid value",
  "path": "/api/v1/employees",
  "validationErrors": {
    "status": "must be one of: ACTIVE, INACTIVE, ON_LEAVE"
  }
}
```

An invalid status query parameter also lists permitted values and identifies `status` as the parameter. Malformed employee IDs identify the `id` parameter. Duplicate email responses return `409 Conflict` and name the conflicting email; unknown employees return `404 Not Found`. An unmapped endpoint returns `404`; an unsupported HTTP method returns `405 Method Not Allowed` with an `Allow` header.

HTTP status mapping: `400` invalid input, `404` employee or endpoint not found, `405` unsupported HTTP method, `409` duplicate email/data conflict, `503` database unavailable, and `500` unexpected or other database operation failure. Internal exception details are written to the application log; they are not returned to API clients.

## Postman

Import [the Postman collection](postman/Employee%20Management%20API.postman_collection.json) into Postman. The collection uses `baseUrl` (`http://localhost:8080`) and automatically saves the created employee ID as `employeeId` for follow-up requests. Request descriptions document expected status codes and response shapes; Postman tests check success responses, pagination metadata, validation details, and allowed status values. Run the requests against a running API and database.

## Architecture and design notes

- **Controller:** owns HTTP routes, request binding, validation, and response status codes. It delegates business work instead of accessing repositories directly.
- **Service:** owns employee use cases and transaction boundaries. It normalizes search input, checks duplicate email addresses, validates supported sort fields, and enforces a maximum page size of 100.
- **Repository:** uses Spring Data JPA for persistence. The search query combines optional status and case-insensitive substring filters, and PostgreSQL performs the paging and sorting.
- **DTOs and mapper:** keep the API contract separate from the JPA entity. Request validation runs at the HTTP boundary; the mapper trims values and stores emails in canonical lowercase form.
- **Entity:** maps employee fields and database constraints. Lifecycle callbacks set timestamps, while enum values are stored as strings so database rows remain readable.
- **Exception advice:** maps failures to consistent, field-specific 400, 404, 405, and 409 responses; database unavailability returns 503, while other database and unexpected failures return 500. Unexpected failures are logged with the request method and path while the response avoids exposing internal details.

For a walkthrough, trace `POST /api/v1/employees` from `EmployeeController` to `EmployeeService`, `EmployeeMapper`, and `EmployeeRepository`. The same layering applies to reads and updates. `Pageable` carries page, size, and sort into the repository, so the application does not load the full employee table into memory.

For list requests, Swagger exposes `page`, `size`, and `sort` query parameters. Use values such as `page=0`, `size=20`, and `sort=lastName,asc`. Sortable fields are listed in the endpoint description; unsupported fields and page sizes above 100 return `400 Bad Request`.

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

Tests cover service behavior, all employee routes, health checks, request validation, and paging parameter/response behavior. Run all checks with `mvn clean verify`.
