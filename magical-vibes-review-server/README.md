# Card review desk

A standalone review queue and dashboard. Runs review each implemented Java card class once, regardless of how many sets register it. Each run has its own immutable model and reasoning level, tasks, attempts, and findings.

## Start the server

From the repository root:

```powershell
.\gradlew.bat :magical-vibes-review-server:bootRun
```

Open [the dashboard](http://localhost:8091). Create a named run, set the exact Codex model identifier and reasoning level, then make it active. The first run is activated automatically. Pausing or switching the active run affects new claims; existing reviews can finish.

To run the standalone package instead:

```powershell
.\gradlew.bat :magical-vibes-review-server:bootJar
java -jar magical-vibes-review-server/build/libs/magical-vibes-review-server.jar
```

The service binds to `127.0.0.1:8091` and has no authentication. Configuration is available through Spring Boot arguments:

```powershell
java -jar magical-vibes-review-server/build/libs/magical-vibes-review-server.jar --server.port=8092 --review.repository-root=C:/path/to/checkout --review.database-path=C:/path/to/reviews.sqlite
```

`REVIEW_REPOSITORY_ROOT` and `REVIEW_DATABASE_PATH` environment variables also work. Relative database paths resolve against the configured repository root. When no root is configured, the server searches upward from its working directory for `settings.gradle.kts`.

Run creation scans the server checkout's current source files, including new implementations added after startup. Keep that checkout up to date before creating a run. Names are enriched from the local Card Info cache when available, then updated from submitted reviews. Creating a run requires no oracle downloads or game-engine startup.

## Start workers

In each clean checkout on `main`, with Git, Codex, and Windows PowerShell on `PATH`:

```powershell
.\scripts\review-worker.ps1 -ServerUrl http://localhost:8091
```

The worker ID defaults to the checkout folder name; override with `-WorkerId`. Start one worker per checkout. A local lock prevents two worker scripts from using the same checkout simultaneously. Workers remain running when the queue is empty and take work from whichever run the dashboard activates next. Stop with Ctrl+C.

Workers report input, cached input, and output tokens from Codex's JSON usage events, and an estimated token cost in USD. The estimate uses `(input - cached input) × input rate + cached input × cached rate + output × output rate`, with rates per million tokens from [OpenAI pricing](https://developers.openai.com/api/docs/pricing). Defaults are in `scripts/review-model-pricing.json`; use `-PricingPath C:/path/to/pricing.json` for another rate table, including custom model identifiers. Prices are loaded at worker startup; restart workers after changing them.

This is an API token estimate, not a billing total for a ChatGPT subscription. Defaults use standard short-context rates and exclude tool fees, cache-write premiums, long-context premiums, service-tier premiums, and regional surcharges. Update the configured rates as needed. Unknown models or missing usage show an unavailable cost rather than zero. Older reviews remain unavailable. Recorded costs persist with each attempt and appear on the card page (including previous attempts) and the run's card table. Upload retries preserve the original estimate without reviewing or charging the estimate twice.

Before each claim, the worker pulls `origin/main`. Tasks supply their model and reasoning level. Reviews inspect shared implementation logic even when the representative printing is a reprint. Production code remains read-only. The same review instructions are shared with the existing set-based script.

Permitted test changes are checked with focused card tests, committed, and pushed directly to `main` even when compilation or test execution fails. Build/tooling failures, two-hour timeouts, failing assertions, and invalid test interaction sequences are accepted for later repair on `main`, even when the card review returns `PASS` with no findings. Validation failures are recorded in the focused validation logs and reported in the worker output; they do not trigger a follow-up review, change the card verdict, or stop the worker. The worker uploads the review result and continues to the next task after publishing the test changes. An unexpected production edit or failed Git publication preserves the checkout and stops the worker after uploading the result. Before every push, the worker fetches `origin/main` and rebases if remote commits are missing locally. Changes arriving between that update and the push are retried with randomized increasing delays (0.5–6 seconds), up to 30 push attempts. Rebase conflicts and push failures without remote advancement stop publication with the local commit preserved. Findings retain only bug descriptions; test code and output are not included in reports.

Worker artifacts live under `magical-vibes-review-server/build/worker`, which is covered by the repository's existing build-directory ignore rule. A `pending-result.json` remains there until the server acknowledges it. Restarting the script retries that upload before doing more work. Rejected results and Codex output are preserved for recovery. Completed findings survive publication failures and remain visible in the dashboard.

Use a separate server checkout, or an external database path, if you want to keep a live database while workers pull and rebase. Workers never stage the database; a manually staged database is also excluded from their commits.

## Progress, comparison, and retrying

- `CREATED`: available to workers.
- `RUNNING`: claimed, with no heartbeat or expiration.
- `COMPLETED`: a review finished, either with a pass or findings.
- `FAILED`: the review could not finish because of an execution failure.

The home page shows individual findings and cards with findings per run, plus totals by model and reasoning level. Execution failures are separate. Model totals sum reported findings; equivalent descriptions in different runs are not automatically merged.

Search for a card name, implementation class, or printing such as `INR 14b`. A card's page groups findings by run/model and retains prior attempts. Every completed review records the Git commit it inspected; runs use evolving `main`, so later models can see earlier test improvements.

Requeue individual reviews from the dashboard, or use a run page's *Recover unfinished work* panel to requeue its failed tasks, its running tasks, its completed tasks whose test changes failed to push, or failed and running tasks at once with *Reset unfinished to Queued*. To finish a run after failures: stop all workers, reset unfinished tasks, then restart the workers. Requeueing invalidates the old attempt token. Late results are rejected instead of overwriting a newer attempt. Counts use each task's latest accepted completed attempt; previous findings remain in history. Jobs are never automatically reassigned or retried. If a worker fails mid-review, resolve any preserved checkout changes before manually requeueing its task.

## SQLite snapshots

The default database is `magical-vibes-review-server/review.sqlite`. It is deliberately **not** added to `.gitignore`, and the service/worker never commits it automatically. Stop the server before staging and committing a snapshot. Startup applies Liquibase migrations and preserves runs, results, and running claims.

Tests and smoke checks use temporary databases or paths under `build`; no initial production database is shipped.

## HTTP API

| Operation | Endpoint | Body / response |
| --- | --- | --- |
| Overview | `GET /api/overview` | Run metrics and model totals |
| List / create runs | `GET /api/runs`, `POST /api/runs` | Create: `name`, `model`, `reasoningEffort` |
| Run details | `GET /api/runs/{id}` | Configuration, progress, and cost (`pricedCards` counts cards with cost data, for per-card averages) |
| Activate / pause | `PUT /api/active-run` | `runId`, or `null` to pause |
| Claim | `POST /api/tasks/claim` | `workerId`; task/configuration/`attemptToken`, or 204 |
| Submit | `POST /api/tasks/{id}/result` | Result described below; 204 on acceptance |
| Task history | `GET /api/tasks/{id}` | Current task and all attempts/findings |
| Requeue task | `POST /api/tasks/{id}/requeue` | No body |
| Bulk requeue | `POST /api/runs/{id}/requeue` | `status`: `FAILED`, `RUNNING`, `UNFINISHED` (both), or `PUSH_FAILED` (completed with a failed publication); returns `requeued` |
| Run tasks | `GET /api/runs/{id}/tasks` | `query`, `status`, zero-based `page` |
| Search cards | `GET /api/cards` | `query`, zero-based `page` |
| Card comparisons | `GET /api/cards/{id}` | Printings and reviews grouped by run |

Result fields: `attemptToken`, `outcome` (`PASS`, `FINDINGS`, or `ERROR`), `cardName`, `findings` (array of descriptions), `reviewedCommit`, `publicationStatus` (`NOT_REQUIRED`, `PUSHED`, or `FAILED`), `publishedCommit`, `executionError`, and `publicationError`. Unused metadata fields may be null. Completed reviews require a full Git commit hash. Findings must be empty for passes/execution errors and nonempty for findings verdicts. Publication and execution failures require their corresponding error descriptions.

Optional usage fields: `inputTokens`, `cachedInputTokens`, `outputTokens`, and `estimatedCostUsd`. Counts must be supplied together as nonnegative integers, with cached input no greater than total input. Cost must be nonnegative and requires usage; it may be null when pricing is unknown. Legacy workers may omit all four fields. Task and card history endpoints return these fields for current and previous attempts, including execution/publication failures.

Repeated identical submissions are accepted without duplicating findings. Invalid input returns 400, unknown records 404, and stale/conflicting submissions 409. Paged endpoints return `items`, `total`, `page`, and `pageSize` (50).

## Verification

```powershell
.\gradlew.bat :magical-vibes-review-server:test :magical-vibes-review-server:bootJar
powershell.exe -NoProfile -File scripts/test-review-worker.ps1
```

Server tests use real temporary SQLite databases. Worker checks mock Codex and use temporary local Git repositories; they do not invoke a model or push to the project's remote.
