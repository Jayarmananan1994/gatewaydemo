# Spring Cloud Gateway Example

This project demonstrates the use of Spring Cloud Gateway to create a simple API gateway for routing and filtering requests.

## Building the Project

To build the project, follow these steps:


1. Navigate to the project directory:

   ```bash
   cd gatewaydemo
   ```

2. Build the project using Gradle:

   ```bash
   ./gradlew build
   ```

## Running the Application

After building the project, you can run the Spring Boot application using the following Gradle command:

```bash
  ./gradlew bootRun
```

The application will start, and you can access the API gateway at `http://localhost:8080`.

## Creating a Mock Subsystem

The repo does not include the source code for downstream service. It is actually pretty simple GET REST endpoint. The below is the content.
```java
@GetMapping(value = "api/users", produces = {"application/json"})
public String userList(@RequestParam("page") int page,
                       @RequestParam(required = false) Long delay) throws InterruptedException {

    return "{\"page\":2,\"per_page\":6,\"total\":12,\"total_pages\":2,\"data\":[{\"id\":7,\"email\":\"michael.lawson@reqres.in\",\"first_name\":\"Michael\",\"last_name\":\"Lawson\",\"avatar\":\"https://reqres.in/img/faces/7-image.jpg\"},...],\"support\":{\"url\":\"https://reqres.in/#support-heading\",\"text\":\"To keep ReqRes free, contributions towards server costs are appreciated!\"}}";
}
```

### Endpoint Details:

- **Path:** `/user-service/users`
- **Method:** `GET`
- **Query Parameters:**
    - `page` (required): Page number for user list pagination.
    - `delay` (optional): Delay in seconds for simulating a delayed response.

Example Request:

```bash
curl http://localhost:8080/api/user-service/users?page=1
```
