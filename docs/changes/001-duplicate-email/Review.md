# 001 - Review

**Status:** Ready for approval

## Acceptance Criteria
| Criterion | Met? | Evidence |
|---|---|---|
| POST with existing email -> 409, not created | Yes | `UserServiceTest.createUserThrowsWhenEmailTaken`, `UserControllerTest.createUserReturns409WhenEmailExists`, smoke #6 |
| PUT to another user's email -> 409, not changed | Yes | `UserServiceTest.updateUserThrowsWhenEmailBelongsToAnotherUser`, `UserControllerTest.updateUserReturns409WhenEmailExists`, smoke #9 |
| PUT keeping own email -> 200 | Yes | `UserServiceTest.updateUserAllowsKeepingOwnEmail`, smoke #8 |
| Case-insensitive | Yes | `UserRepositoryTest.findByEmailIgnoresCase`, smoke #7 (`ALICE@EXAMPLE.COM` -> 409) |
| Null emails not duplicates | Yes | `UserServiceTest.createUserWithNullEmailSkipsDuplicateCheck`, `UserRepositoryTest.findByEmailSkipsUsersWithoutEmail` |
| Message names the email | Yes | Response: `"message":"Email already in use: alice@example.com"` |
| Existing tests pass, coverage 100% | Yes | 45/45 pass, JaCoCo 0 missed lines/branches |

## Test Results
- Unit/MVC tests: 45/45 (was 35; +10 new)
- Failures: none

## Coverage (JaCoCo)
| Class | Lines missed | Branches missed |
|---|---|---|
| DuplicateEmailException (new) | 0 | 0 |
| UserRepository | 0 | 0 (8/8) |
| UserService | 0 | 0 (14/14) |
| UserController, User, UserNotFoundException, SpringaiApplication | 0 | 0 |

## Smoke Test
- 15/15 checks passed on a real app instance (port 8089), including 3 new 409 checks
- Final count after create + delete = 3, as expected

## Changed Files
| File | Change |
|---|---|
| `exception/DuplicateEmailException.java` | New, `@ResponseStatus(CONFLICT)` |
| `repository/UserRepository.java` | `findByEmail` (case-insensitive, null-safe) |
| `service/UserService.java` | `ensureEmailAvailable` used by `createUser` and `updateUser` |
| `UserRepositoryTest`, `UserServiceTest`, `UserControllerTest` | +4, +4, +2 tests |
| `docs/changes/001-duplicate-email/*` | Requirement, Task, Review |

## Self-Review Findings
- Rules (Java 8, layering, injection, status codes): ok. Lookup in repository, rule in service, controller unchanged
- Edge cases and null handling: ok. Null email skipped, own email allowed on update, check runs before any field is changed
- Security: the error JSON includes a `trace` field (stack trace). This is pre-existing behavior from `spring-boot-devtools` and applies to all errors, not only 409. Not active when running the packaged jar
- Leftover debug code or secrets: none

## Risks for Sign-off
- Check-then-save is not atomic under concurrent requests (accepted for in-memory demo; use a DB unique constraint later)
- Smoke-test script update and `docs/Skills.md` learnings are pending the stash restore
