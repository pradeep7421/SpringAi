# Springai - Project Instructions

## Tech stack
- Java 1.8 and Spring Boot 2.7.18 (Maven, using the `mvnw` wrapper)
- Starters: `spring-boot-starter-web`, `spring-boot-starter-test`, `spring-boot-devtools`
- No database yet: JPA dependencies are commented out in `pom.xml`; data is kept in memory

## Build and run
- Compile: `.\mvnw.cmd compile`
- Run: `.\mvnw.cmd spring-boot:run` (port 8080)
- Test: `.\mvnw.cmd test`

## Project structure (`com.springai`)
- `model` - plain Java classes (e.g. `User`)
- `repository` - in-memory repositories using `ConcurrentHashMap` (e.g. `UserRepository`, seeded with 3 users)
- `service` - business logic (e.g. `UserService`)
- `controller` - REST controllers (e.g. `UserController` at `/api/users`)
- `exception` - custom exceptions mapped to HTTP status via `@ResponseStatus`

## Where to make changes
- New endpoint: controller method + service method + update the endpoint table below
- Business rules and validation (e.g. duplicate checks): service layer
- Data storage and lookups: repository only
- New error type: new class in `exception`, annotated with `@ResponseStatus`
- New field on a resource: model class (getter/setter) + service update logic

## Coding rules
- Code must compile on Java 8: do not use `var`, records, `List.of()`, `Map.of()`, text blocks, or switch expressions
- Use constructor injection, not field `@Autowired`
- No Lombok: write getters/setters explicitly
- Keep the layering: controller -> service -> repository; controllers never call repositories directly
- Throw a custom exception for missing resources (e.g. `UserNotFoundException` -> 404)
- Return `201 Created` for POST and `204 No Content` for DELETE

## API endpoints
| Method | Path | Description |
|---|---|---|
| GET | `/api/users` | List all users |
| GET | `/api/users/count` | Get total number of users |
| GET | `/api/users/{id}` | Get user by id |
| POST | `/api/users` | Create user |
| PUT | `/api/users/{id}` | Update user |
| DELETE | `/api/users/{id}` | Delete user |
| GET | `/api/users/search?name=` | Search users by name (contains, case-insensitive) |

When adding a new endpoint, update this table.
