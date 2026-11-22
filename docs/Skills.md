# Project Learnings

Reusable patterns and pitfalls. Read at the start of every change (Stage 1 of `/dev-workflow`). Keep entries short; remove outdated ones.

## Patterns
- New GET endpoint with a literal path (e.g. `/count`, `/search`): declare it so it is not confused with `/{id}`; Spring prefers literal paths, but keep them grouped above `/{id}` for readability.
- Search/filter: loop in the service, null-check the field, compare `toLowerCase()` on both sides, return `List` (empty list, never `null`).
- Missing resource: throw a custom exception annotated with `@ResponseStatus` instead of returning `null`.
- Service tests: Mockito (`@ExtendWith(MockitoExtension.class)`, `@Mock` repository, `@InjectMocks` service). Controller tests: `@WebMvcTest` + `@MockBean` service + MockMvc.
- Uniqueness rule (e.g. email, #001): repository `findByX` lookup (null-safe, `equalsIgnoreCase`) + private service guard `ensureXAvailable(value, currentId)`; pass `null` on create and the user's id on update so keeping your own value is allowed. Run the guard before changing any fields.
- Mockito returns `Optional.empty()` for unstubbed `Optional` methods, so adding a new lookup doesn't break existing service tests.

## Pitfalls
- Java 8 only: no `var`, `List.of()`, records, text blocks. Use `Arrays.asList`, `Collections.singletonList`.
- `UserService.updateUser` copies fields one by one: a new field on `User` must also be copied there, or updates silently ignore it.
- `UserRepository` constructor calls `save()`: any bug in `save()` crashes app startup and every Spring test.
- PowerShell + `curl.exe`: `\"` inside JSON is sent literally (400 error). Use single-quoted JSON, `Invoke-RestMethod`, or `--data-binary @file`.
- Quote URLs with `?` in PowerShell: `curl.exe "http://localhost:8080/api/users/search?name=a"`.
- Git: `git rebase --exec "git commit --amend ..."` stops on empty commits; add `--allow-empty`.
- Git: a remote added without a fetch refspec hides remote branches; check with `git ls-remote --heads origin`.
- Error responses include a `trace` (stack trace) while `spring-boot-devtools` is active; don't rely on it and don't expose it in production.
- Stashing workflow files (`git stash -u`) also hides `docs/Skills.md` and the smoke script; commit them first instead.

## Useful Commands
- Tests + coverage: `.\mvnw.cmd clean test`, then open `target/site/jacoco/index.html`.
- Smoke test: `.\.github\skills\dev-workflow\scripts\smoke-test.ps1` (uses port 8089, so it doesn't clash with an app on 8080).
