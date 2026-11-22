# 001 - Plan

**Branch:** `change/001-duplicate-email`
**Status:** Done

## Tasks
- [x] 1. `exception/DuplicateEmailException.java` (new): `@ResponseStatus(HttpStatus.CONFLICT)`, message `Email already in use: <email>`. Proven by: `UserControllerTest.createUserReturns409WhenEmailExists`
- [x] 2. `repository/UserRepository.java`: add `Optional<User> findByEmail(String email)`, case-insensitive, skips users with null email. Proven by: `UserRepositoryTest.findByEmail*`
- [x] 3. `service/UserService.java`: private `ensureEmailAvailable(String email, Long currentUserId)`; call it in `createUser` (id null) and `updateUser` (id = updated user). Null email skips the check. Proven by: `UserServiceTest` duplicate/same-user/null cases
- [x] 4. Tests:
  - `UserRepositoryTest`: found (case-insensitive), not found, null email argument, users with null email skipped
  - `UserServiceTest`: create duplicate -> exception and no save; update to other user's email -> exception and no save; update keeping own email -> ok; null email -> no lookup; existing create/update tests stub `findByEmail`
  - `UserControllerTest`: POST -> 409, PUT -> 409
- [x] 5. Compile and run full tests; coverage must stay 100%
- [x] 6. `.github/copilot-instructions.md`: no change needed (no new endpoint, rule already covered by "Business rules ... duplicate checks: service layer")
- [x] 7. Smoke test: add `POST duplicate email -> 409` check after the stash is restored (script is stashed)

## Out of Scope
- Input validation (blank/invalid email) - separate change
- Database unique constraint / concurrency guarantees

## Plan Changes During Execution
- Existing create/update service tests needed no extra stubbing: Mockito returns `Optional.empty()` for unstubbed `findByEmail`.
- Smoke test was run manually with the 409 cases added (script is stashed).
