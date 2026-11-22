---
name: dev-workflow
description: 'End-to-end change workflow for this project with human approval gates. Use when: new requirement, new feature, bug fix, change request, enhancement. Produces Requirement.md, Task.md, Review.md per change, updates Skills.md learnings, runs full tests and smoke tests, then auto-commits after commit approval and auto-pushes to GitHub after push approval.'
argument-hint: 'Describe the requirement or bug'
---

# Dev Workflow: Requirement to Push

Run every stage in order. **Never skip a HUMAN APPROVAL gate.** At each gate, ask the user (use the ask-questions tool if available), then STOP and wait. If the user rejects or requests changes, revise the current stage and ask again.

## Approval Gates
| Gate | After stage | On approval the agent automatically... |
|---|---|---|
| Requirement | 1 | writes the plan |
| Plan | 2 | executes tasks, updates rules, runs tests, writes Review.md |
| Commit | 5 | saves learnings, commits, merges to master, re-tests |
| Push | 7c | pushes to GitHub and verifies |

## Paths
- Change folder: `docs/changes/<NNN>-<short-slug>/` (NNN = next 3-digit number, e.g. `003-duplicate-email`)
- Learnings: [docs/Skills.md](../../../docs/Skills.md)
- Project rules and architecture: [copilot-instructions.md](../../copilot-instructions.md)
- Templates: [Requirement](./assets/Requirement.template.md), [Task](./assets/Task.template.md), [Review](./assets/Review.template.md)
- Smoke test: [smoke-test.ps1](./scripts/smoke-test.ps1)

## Stage 0: Pre-checks
1. Run `git status -sb`. If there are uncommitted changes, ask the user whether to commit, stash, or stop. Do not discard anything.
2. Check git identity is set: `git config user.name` and `git config user.email`. If empty, ask the user before continuing.
3. Check the remote: `git remote -v` must point to the project's GitHub repo.
4. Run `git pull` on `master` to start from the latest code.
5. Create a branch: `git checkout -b change/<NNN>-<short-slug>`.

## Stage 1: ANALYZE -> Requirement.md
1. Read `docs/Skills.md` first and reuse any matching pattern or past pitfall.
2. Read `.github/copilot-instructions.md` for architecture, rules, endpoints.
3. Search the code (targeted search, not every file) to find affected classes. Read only those.
4. Fill `Requirement.md` from the template: problem, acceptance criteria (testable), affected files with reason, risks/breakage areas, open questions.
5. **HUMAN APPROVAL:** show a short summary and ask "Approve requirement analysis?"

## Stage 2: PLAN -> Task.md
1. Break the work into small ordered tasks. Each task names the exact file and change, and the test that proves it.
2. Include: code tasks, test tasks (coverage must not drop), doc/rule updates, smoke-test updates for new endpoints.
3. **HUMAN APPROVAL:** "Approve plan?"

## Stage 3: EXECUTE
1. Do tasks in order; tick each `[x]` in `Task.md` as it is done.
2. Follow every rule in `copilot-instructions.md` (Java 8, layering, constructor injection, status codes).
3. After each code task, compile: `.\mvnw.cmd -q compile`. Fix errors before moving on.
4. If something unexpected changes the plan, update `Task.md` and tell the user.

## Stage 4: ARCHITECTURE / RULE UPDATE
1. Update `.github/copilot-instructions.md` if anything changed: endpoints table, packages, new rules, "Where to make changes".
2. If a new endpoint was added, add its check to `scripts/smoke-test.ps1`.

## Stage 5: TEST + REVIEW -> Review.md
1. Full test suite with coverage: `.\mvnw.cmd clean test`. All tests must pass.
2. Coverage: read `target/site/jacoco/jacoco.csv`; report missed lines/branches per class. Coverage must not drop below the previous level (currently 100%).
3. Smoke test against the running app: `.\.github\skills\dev-workflow\scripts\smoke-test.ps1`. All checks must pass.
4. Self-review the diff (`git diff master...HEAD`): rule violations, null handling, edge cases, security (OWASP), leftover debug code, secrets.
5. Fill `Review.md`: acceptance criteria met (yes/no each), test results, coverage, smoke results, diff summary, risks.
6. If anything fails, go back to Stage 3. Do not hide failures.
7. **HUMAN APPROVAL (commit gate):** show `Review.md` summary and the changed-file list. Ask "Approve changes for commit?" with options:
   - **Approve and commit** -> continue to Stage 6, then auto-commit in Stage 7
   - **Request changes** -> back to Stage 3
   - **Stop** -> leave changes uncommitted on the branch

## Stage 6: SAVE LEARNINGS -> docs/Skills.md
Add only reusable knowledge (1-3 bullets): a pattern that worked, a pitfall hit, a command that helped. Update or remove outdated entries. Keep it short.

## Stage 7: AUTO-COMMIT, MERGE, AUTO-PUSH

### 7a. Auto-commit (runs automatically once the commit gate is approved; no extra prompt)
1. Stage everything for this change: `git add -A`.
2. Safety check on staged files: `git diff --cached --name-only`. Abort and tell the user if any match `target/`, `*.jar`, `*.class`, `*.zip`, `.env`, or contain secrets (passwords, tokens, keys). Unstage them with `git restore --staged <file>`.
3. The commit must include code, tests, `docs/changes/<NNN>-<slug>/` files, `docs/Skills.md`, and any rule/smoke-test updates.
4. Commit with this message format:
   ```
   git commit -m "<type>: <short imperative summary> (#<NNN>)" -m "Requirement: docs/changes/<NNN>-<slug>/Requirement.md" -m "Tests: <passed>/<total>, coverage <x>%, smoke <passed>/<total>"
   ```
   `<type>` is one of `feat`, `fix`, `test`, `docs`, `refactor`, `chore`.
5. Show the result: `git log -1 --format="%h | %an <%ae> | %s"`.

### 7b. Merge to master (automatic)
1. `git checkout master`
2. `git merge --no-ff change/<NNN>-<slug> -m "Merge change <NNN>: <title>"`
3. If the merge conflicts: stop, show the conflicting files, and ask the user. Never auto-resolve by discarding changes.
4. Re-run `.\mvnw.cmd test` on master. If it fails, stop and report; do not push.

### 7c. HUMAN APPROVAL (push gate)
Show what will be pushed: `git log origin/master..HEAD --oneline` and the target (`git remote get-url origin`, branch `master`). Ask "Push to GitHub?" with options:
- **Push now** -> 7d
- **Keep local** -> stop; report that the commit is local only and can be pushed later with "push my changes"

### 7d. Auto-push (runs automatically once the push gate is approved)
1. `git push`
2. If a browser sign-in opens, tell the user to sign in with the GitHub account that owns the repo.
3. If the push is rejected because GitHub has newer commits: `git pull --rebase`, re-run `.\mvnw.cmd test`, then ask the push gate again. If the rebase conflicts, stop and ask the user.
4. Verify: `git status -sb` must show `master...origin/master` with no ahead/behind, and `git log origin/master -1 --oneline` must show the new merge commit.
5. Clean up: `git branch -d change/<NNN>-<slug>`.
6. Never force push, never use `--no-verify`, never put passwords or tokens in commands or URLs.

## Stage 8: NEXT SIMILAR REQUIREMENT
Start again at Stage 0. Stage 1 always reads `docs/Skills.md` so past learnings are reused.

## Final report to the user
Change id, files changed, tests (pass/total), coverage, smoke result, commit hash, merge commit hash, pushed yes/no, GitHub commit link (`https://github.com/<owner>/<repo>/commit/<hash>`).
