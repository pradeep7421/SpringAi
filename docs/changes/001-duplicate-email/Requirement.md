# 001 - Duplicate emails return 409 Conflict

**Type:** Feature
**Status:** Draft

## Problem / Request
Two users can currently have the same email. Creating or updating a user with an email that already belongs to another user must be rejected with `409 Conflict`.

## Acceptance Criteria
- [ ] `POST /api/users` with an email already used by another user returns `409` and the user is not created
- [ ] `PUT /api/users/{id}` changing the email to one used by another user returns `409` and the user is not changed
- [ ] `PUT /api/users/{id}` keeping the user's own current email still returns `200`
- [ ] Email comparison ignores upper/lower case (`ALICE@example.com` = `alice@example.com`)
- [ ] Users with no email (`null`) are not treated as duplicates of each other
- [ ] Error message names the email, e.g. `Email already in use: alice@example.com`
- [ ] All existing tests still pass; coverage stays at 100%

## Current Behavior
`POST /api/users` with `{"email":"alice@example.com", ...}` returns `201` and creates a second user with Alice's email.

## Affected Files
| File | Why |
|---|---|
| `exception/DuplicateEmailException.java` (new) | New error type, `@ResponseStatus(HttpStatus.CONFLICT)` |
| `repository/UserRepository.java` | New lookup `findByEmail(String)` (lookups belong in the repository) |
| `service/UserService.java` | Business rule: check duplicates in `createUser` and `updateUser` |
| `service/UserServiceTest.java` | Tests for duplicate / same-user / null email cases |
| `repository/UserRepositoryTest.java` | Tests for `findByEmail` |
| `controller/UserControllerTest.java` | Tests that POST/PUT map the exception to `409` |

No controller code change and no new endpoint.

## Reused Learnings (from docs/Skills.md)
- Custom exception with `@ResponseStatus` (same as `UserNotFoundException` -> 404)
- Null-check fields before comparing; compare case-insensitively
- Service tests with Mockito, controller tests with `@WebMvcTest`
- Java 8 only

## Risks / Possible Breakage
- `updateUser` must exclude the user being updated, or saving an unchanged email would wrongly return 409
- Check-then-save is not atomic: two simultaneous requests with the same new email could both pass. Acceptable for the in-memory demo; a real database should use a unique constraint
- Seed data has unique emails, so startup is unaffected

## Workflow Note
The workflow setup files (`docs/Skills.md`, smoke test, templates) are stashed (`stash@{0}`). Learnings and the smoke-test 409 check will be added after the stash is restored, to avoid conflicts.

## Open Questions
- Case-insensitive comparison: assumed **yes**
- Trim spaces before comparing: assumed **no** (input validation is a separate change)
