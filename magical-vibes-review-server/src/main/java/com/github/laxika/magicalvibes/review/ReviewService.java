package com.github.laxika.magicalvibes.review;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/** Coordinates persistent runs and claims; all changes to ownership and results are transactional. */
@Service
public class ReviewService {
    private static final Set<String> EFFORTS = Set.of("low", "medium", "high", "xhigh", "max");
    private static final String RUNS = """
            SELECT r.id, r.name, r.model, r.reasoning_effort AS reasoningEffort,
                   r.catalog_commit AS catalogCommit, r.created_at AS createdAt,
                   CASE WHEN r.id = (SELECT active_run_id FROM review_settings WHERE id=1) THEN 1 ELSE 0 END AS active,
                   COUNT(t.id) AS total,
                   COALESCE(SUM(t.status='CREATED'),0) AS created,
                   COALESCE(SUM(t.status='RUNNING'),0) AS running,
                   COALESCE(SUM(t.status='COMPLETED'),0) AS completed,
                   COALESCE(SUM(t.status='FAILED'),0) AS failed,
                   COALESCE(SUM(t.status='COMPLETED' AND a.outcome='FINDINGS'),0) AS cardsWithFindings,
                   COALESCE(SUM(CASE WHEN t.status='COMPLETED' THEN f.total ELSE 0 END),0) AS findingCount,
                   COALESCE(SUM(a.publication_status='FAILED'),0) AS publicationFailures,
                   SUM(costs.estimatedCostUsd) AS estimatedCostUsd,
                   COALESCE(SUM(costs.missingCostCount),0) AS missingCostCount,
                   COALESCE(SUM(costs.estimatedCostUsd IS NOT NULL),0) AS pricedCards
            FROM review_run r
            LEFT JOIN review_task t ON t.run_id=r.id
            LEFT JOIN review_attempt a ON a.token=t.current_attempt
            LEFT JOIN (SELECT attempt_token,COUNT(*) AS total FROM review_finding GROUP BY attempt_token) f
                   ON f.attempt_token=a.token
            LEFT JOIN (SELECT task_id,SUM(estimated_cost_usd) AS estimatedCostUsd,
                              SUM(finished_at IS NOT NULL AND estimated_cost_usd IS NULL) AS missingCostCount
                       FROM review_attempt GROUP BY task_id) costs ON costs.task_id=t.id
            """;
    private static final String TASKS = """
            SELECT t.id,t.card_id AS cardId,t.class_name AS className,c.display_name AS cardName,
                   t.source_path AS sourcePath,t.set_code AS setCode,t.collector_number AS collectorNumber,
                   t.status,a.worker_id AS workerId,a.started_at AS startedAt,a.finished_at AS finishedAt,
                   a.outcome,a.reviewed_commit AS reviewedCommit,a.publication_status AS publicationStatus,
                   a.published_commit AS publishedCommit,a.execution_error AS executionError,
                   a.publication_error AS publicationError,
                   a.input_tokens AS inputTokens,a.cached_input_tokens AS cachedInputTokens,
                   a.output_tokens AS outputTokens,a.estimated_cost_usd AS estimatedCostUsd,
                   (SELECT COUNT(*) FROM review_finding WHERE attempt_token=a.token) AS findingCount
            FROM review_task t JOIN review_card c ON c.id=t.card_id
            LEFT JOIN review_attempt a ON a.token=t.current_attempt
            """;
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transactions;
    private final CardSourceCatalog catalog;
    private final Path root;
    private final ObjectMapper mapper;

    public ReviewService(JdbcTemplate jdbc, TransactionTemplate transactions, CardSourceCatalog catalog,
                         Path reviewRepositoryRoot, ObjectMapper mapper) {
        this.jdbc = jdbc;
        this.transactions = transactions;
        this.catalog = catalog;
        this.root = reviewRepositoryRoot;
        this.mapper = mapper;
    }

    public Map<String, Object> createRun(NewRun request) throws IOException {
        require(request != null, "Run configuration is required");
        requireText(request.name(), "Run name", 200);
        requireText(request.model(), "Model", 200);
        require(request.reasoningEffort() != null && EFFORTS.contains(request.reasoningEffort()), "Unsupported reasoning effort");
        var cards = catalog.scan();
        require(!cards.isEmpty(), "No implemented card registrations found in the configured checkout");
        String commit = catalogCommit();
        return transactions.execute(transaction -> {
            long runId = insertId("INSERT INTO review_run(name,model,reasoning_effort,catalog_commit,created_at) VALUES (?,?,?,?,?) RETURNING id",
                    request.name().trim(), request.model().trim(), request.reasoningEffort(), commit, Instant.now().toString());
            jdbc.batchUpdate("INSERT INTO review_card(class_name,display_name) VALUES (?,?) ON CONFLICT(class_name) DO NOTHING",
                    cards, 500, (statement, card) -> {
                        statement.setString(1, card.className());
                        statement.setString(2, card.displayName());
                    });
            Map<String, Long> cardIds = jdbc.query("SELECT id,class_name FROM review_card", rows -> {
                Map<String, Long> ids = new java.util.HashMap<>();
                while (rows.next()) {
                    ids.put(rows.getString("class_name"), rows.getLong("id"));
                }
                return ids;
            });
            jdbc.batchUpdate("INSERT INTO review_task(run_id,card_id,class_name,source_path,set_code,collector_number) VALUES (?,?,?,?,?,?)",
                    cards, 500, (statement, card) -> {
                        var printing = card.printings().getFirst();
                        statement.setLong(1, runId);
                        statement.setLong(2, cardIds.get(card.className()));
                        statement.setString(3, card.className());
                        statement.setString(4, card.sourcePath());
                        statement.setString(5, printing.setCode());
                        statement.setString(6, printing.collectorNumber());
                    });
            jdbc.update("DELETE FROM review_printing WHERE card_id IN (SELECT card_id FROM review_task WHERE run_id=?)", runId);
            var printings = cards.stream().flatMap(card -> card.printings().stream()
                    .map(printing -> new PrintingInsert(cardIds.get(card.className()), printing))).toList();
            jdbc.batchUpdate("INSERT INTO review_printing(card_id,set_code,collector_number) VALUES (?,?,?)",
                    printings, 500, (statement, printing) -> {
                        statement.setLong(1, printing.cardId());
                        statement.setString(2, printing.printing().setCode());
                        statement.setString(3, printing.printing().collectorNumber());
                    });
            jdbc.update("UPDATE review_settings SET active_run_id=? WHERE id=1 AND (SELECT COUNT(*) FROM review_run)=1", runId);
            return run(runId);
        });
    }

    public List<Map<String, Object>> runs() {
        return jdbc.queryForList(RUNS + " GROUP BY r.id ORDER BY r.id DESC");
    }

    public Map<String, Object> run(long id) {
        return one(RUNS + " WHERE r.id=? GROUP BY r.id", id);
    }

    public Map<String, Object> overview() {
        List<Map<String, Object>> runs = runs();
        Map<String, Map<String, Object>> groups = new LinkedHashMap<>();
        for (var run : runs) {
            String key = run.get("model") + ":" + run.get("reasoningEffort");
            var group = groups.computeIfAbsent(key, ignored -> {
                var value = new LinkedHashMap<String, Object>();
                value.put("model", run.get("model"));
                value.put("reasoningEffort", run.get("reasoningEffort"));
                value.put("runs", 0L);
                value.put("completed", 0L);
                value.put("findingCount", 0L);
                value.put("cardsWithFindings", 0L);
                return value;
            });
            group.put("runs", number(group.get("runs")) + 1);
            for (String metric : List.of("completed", "findingCount", "cardsWithFindings")) {
                group.put(metric, number(group.get(metric)) + number(run.get(metric)));
            }
        }
        return Map.of("runs", runs, "models", new ArrayList<>(groups.values()));
    }

    public void activate(Long id) {
        transactions.executeWithoutResult(transaction -> {
            if (id != null) {
                one("SELECT id FROM review_run WHERE id=?", id);
            }
            jdbc.update("UPDATE review_settings SET active_run_id=? WHERE id=1", id);
        });
    }

    public Map<String, Object> claim(Claim request) {
        require(request != null, "Worker identity is required");
        requireText(request.workerId(), "Worker ID", 200);
        return transactions.execute(transaction -> {
            var pending = jdbc.queryForList("""
                    SELECT t.id,t.run_id AS runId,r.name AS runName,r.model,r.reasoning_effort AS reasoningEffort,
                           t.card_id AS cardId,c.display_name AS cardName,t.class_name AS className,
                           t.source_path AS sourcePath,t.set_code AS setCode,t.collector_number AS collectorNumber
                    FROM review_task t JOIN review_run r ON r.id=t.run_id JOIN review_card c ON c.id=t.card_id
                    WHERE t.run_id=(SELECT active_run_id FROM review_settings WHERE id=1) AND t.status='CREATED'
                    ORDER BY t.class_name LIMIT 1
                    """);
            if (pending.isEmpty()) {
                return null;
            }
            Map<String, Object> task = pending.getFirst();
            String token = UUID.randomUUID().toString();
            jdbc.update("INSERT INTO review_attempt(token,task_id,worker_id,started_at) VALUES (?,?,?,?)",
                    token, task.get("id"), request.workerId().trim(), Instant.now().toString());
            jdbc.update("UPDATE review_task SET status='RUNNING',current_attempt=? WHERE id=?", token, task.get("id"));
            task.put("attemptToken", token);
            return task;
        });
    }

    public void submit(long taskId, Result result) {
        validate(result);
        String serialized = mapper.writeValueAsString(result);
        transactions.executeWithoutResult(transaction -> {
            var task = one("SELECT current_attempt,status,card_id FROM review_task WHERE id=?", taskId);
            if (!result.attemptToken().equals(task.get("current_attempt"))) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "This attempt was manually requeued; its result is no longer current");
            }
            var attempt = one("SELECT result_json FROM review_attempt WHERE token=?", result.attemptToken());
            if (attempt.get("result_json") != null) {
                // Read older payloads with missing optional usage fields as null, so queued retries remain idempotent after migration.
                if (result.equals(mapper.readValue((String) attempt.get("result_json"), Result.class))) {
                    return;
                }
                throw new ResponseStatusException(HttpStatus.CONFLICT, "A different result was already accepted for this attempt");
            }
            jdbc.update("""
                    UPDATE review_attempt SET finished_at=?,outcome=?,reviewed_commit=?,publication_status=?,
                           published_commit=?,execution_error=?,publication_error=?,result_json=?,
                           input_tokens=?,cached_input_tokens=?,output_tokens=?,estimated_cost_usd=? WHERE token=?
                    """, Instant.now().toString(), result.outcome(), result.reviewedCommit(), result.publicationStatus(),
                    result.publishedCommit(), result.executionError(), result.publicationError(), serialized,
                    result.inputTokens(), result.cachedInputTokens(), result.outputTokens(), result.estimatedCostUsd(), result.attemptToken());
            for (String finding : result.findings()) {
                jdbc.update("INSERT INTO review_finding(attempt_token,description) VALUES (?,?)", result.attemptToken(), finding.trim());
            }
            if (result.cardName() != null && !result.cardName().isBlank()) {
                jdbc.update("UPDATE review_card SET display_name=? WHERE id=?", result.cardName().trim(), task.get("card_id"));
            }
            jdbc.update("UPDATE review_task SET status=? WHERE id=?", result.outcome().equals("ERROR") ? "FAILED" : "COMPLETED", taskId);
        });
    }

    public void requeue(long taskId) {
        transactions.executeWithoutResult(transaction -> {
            one("SELECT id FROM review_task WHERE id=?", taskId);
            jdbc.update("UPDATE review_task SET status='CREATED',current_attempt=NULL WHERE id=?", taskId);
        });
    }

    public int requeueRun(long runId, String status) {
        require(status != null && Set.of("FAILED", "RUNNING", "UNFINISHED", "PUSH_FAILED").contains(status),
                "Bulk requeue supports FAILED, RUNNING, UNFINISHED, or PUSH_FAILED tasks");
        return transactions.execute(transaction -> {
            one("SELECT id FROM review_run WHERE id=?", runId);
            if (status.equals("UNFINISHED")) {
                return jdbc.update("UPDATE review_task SET status='CREATED',current_attempt=NULL WHERE run_id=? AND status IN ('RUNNING','FAILED')", runId);
            }
            if (status.equals("PUSH_FAILED")) {
                return jdbc.update("""
                        UPDATE review_task SET status='CREATED',current_attempt=NULL
                        WHERE run_id=? AND status='COMPLETED'
                          AND current_attempt IN (SELECT token FROM review_attempt WHERE publication_status='FAILED')
                        """, runId);
            }
            return jdbc.update("UPDATE review_task SET status='CREATED',current_attempt=NULL WHERE run_id=? AND status=?", runId, status);
        });
    }

    public Map<String, Object> tasks(long runId, String query, String status, int page) {
        one("SELECT id FROM review_run WHERE id=?", runId);
        require(page >= 0, "Page cannot be negative");
        require(status.isBlank() || Set.of("CREATED", "RUNNING", "COMPLETED", "FAILED").contains(status), "Unknown task status");
        String filter = " WHERE t.run_id=? AND (?='' OR t.status=?) AND " + searchFilter();
        String pattern = pattern(query);
        Object[] args = {runId, status, status, pattern, pattern, pattern, pattern};
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM review_task t JOIN review_card c ON c.id=t.card_id" + filter, Long.class, args);
        var params = new ArrayList<>(List.of(args));
        params.add((long) page * 50);
        var rows = jdbc.queryForList(TASKS + filter + " ORDER BY t.class_name LIMIT 50 OFFSET ?", params.toArray());
        return Map.of("items", rows, "total", count, "page", page, "pageSize", 50);
    }

    public Map<String, Object> task(long id) {
        Map<String, Object> task = one(TASKS + " WHERE t.id=?", id);
        task.put("attempts", attempts(id));
        return task;
    }

    public Map<String, Object> cards(String query, int page) {
        require(page >= 0, "Page cannot be negative");
        String filter = """
                 WHERE c.display_name LIKE ? ESCAPE '!' OR c.class_name LIKE ? ESCAPE '!'
                    OR EXISTS (SELECT 1 FROM review_printing p WHERE p.card_id=c.id
                               AND (p.set_code || ' ' || p.collector_number) LIKE ? ESCAPE '!')
                """;
        String pattern = pattern(query);
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM review_card c" + filter, Long.class, pattern, pattern, pattern);
        var rows = jdbc.queryForList("SELECT c.id,c.class_name AS className,c.display_name AS cardName FROM review_card c"
                + filter + " ORDER BY c.display_name LIMIT 50 OFFSET ?", pattern, pattern, pattern, (long) page * 50);
        return Map.of("items", rows, "total", count, "page", page, "pageSize", 50);
    }

    public Map<String, Object> card(long id) {
        var card = one("SELECT id,class_name AS className,display_name AS cardName FROM review_card WHERE id=?", id);
        card.put("printings", jdbc.queryForList("SELECT set_code AS setCode,collector_number AS collectorNumber FROM review_printing WHERE card_id=? ORDER BY set_code,collector_number", id));
        var reviews = jdbc.queryForList(TASKS + " WHERE t.card_id=? ORDER BY t.run_id DESC", id);
        for (var review : reviews) {
            review.put("run", one("SELECT r.id,r.name,r.model,r.reasoning_effort AS reasoningEffort FROM review_run r JOIN review_task t ON t.run_id=r.id WHERE t.id=?", review.get("id")));
            review.put("attempts", attempts(number(review.get("id"))));
        }
        card.put("reviews", reviews);
        return card;
    }

    private List<Map<String, Object>> attempts(long taskId) {
        var attempts = jdbc.queryForList("""
                SELECT a.token,a.worker_id AS workerId,a.started_at AS startedAt,a.finished_at AS finishedAt,
                       a.outcome,a.reviewed_commit AS reviewedCommit,a.publication_status AS publicationStatus,
                       a.published_commit AS publishedCommit,a.execution_error AS executionError,
                       a.publication_error AS publicationError,
                       a.input_tokens AS inputTokens,a.cached_input_tokens AS cachedInputTokens,
                       a.output_tokens AS outputTokens,a.estimated_cost_usd AS estimatedCostUsd,
                       CASE WHEN a.token=t.current_attempt THEN 1 ELSE 0 END AS current
                FROM review_attempt a JOIN review_task t ON t.id=a.task_id WHERE a.task_id=? ORDER BY a.rowid DESC
                """, taskId);
        for (var attempt : attempts) {
            attempt.put("findings", jdbc.queryForList("SELECT description FROM review_finding WHERE attempt_token=? ORDER BY id", String.class, attempt.get("token")));
        }
        return attempts;
    }

    private void validate(Result result) {
        require(result != null, "Result is required");
        requireText(result.attemptToken(), "Attempt token", 100);
        boolean hasUsage = result.inputTokens() != null || result.cachedInputTokens() != null || result.outputTokens() != null;
        if (hasUsage) {
            require(result.inputTokens() != null && result.cachedInputTokens() != null && result.outputTokens() != null,
                    "Token usage must include input, cached input, and output counts");
            require(result.inputTokens() >= 0 && result.cachedInputTokens() >= 0 && result.outputTokens() >= 0
                    && result.cachedInputTokens() <= result.inputTokens(), "Invalid token usage counts");
        }
        if (result.estimatedCostUsd() != null) {
            require(hasUsage && result.estimatedCostUsd().signum() >= 0, "Estimated cost requires token usage and cannot be negative");
            require(Double.isFinite(result.estimatedCostUsd().doubleValue()), "Estimated cost is too large");
        }
        require(result.outcome() != null && Set.of("PASS", "FINDINGS", "ERROR").contains(result.outcome()), "Unknown review outcome");
        require(result.findings() != null, "Findings must be an array");
        require(result.findings().size() <= 1000, "Too many findings");
        for (String finding : result.findings()) {
            requireText(finding, "Finding description", 20000);
        }
        require(result.outcome().equals("FINDINGS") == !result.findings().isEmpty(), "Only FINDINGS results contain findings");
        require(result.publicationStatus() != null && Set.of("NOT_REQUIRED", "PUSHED", "FAILED").contains(result.publicationStatus()), "Unknown publication status");
        if (!result.outcome().equals("ERROR")) {
            require(result.reviewedCommit() != null && result.reviewedCommit().matches("[0-9a-f]{40,64}"), "The reviewed Git commit is required");
        } else {
            requireText(result.executionError(), "Execution failure", 20000);
        }
        if (result.publicationStatus().equals("PUSHED")) {
            require(result.publishedCommit() != null && result.publishedCommit().matches("[0-9a-f]{40,64}"), "The published Git commit is required");
        }
        if (result.publicationStatus().equals("FAILED")) {
            requireText(result.publicationError(), "Publication failure", 20000);
        }
        if (result.cardName() != null) {
            requireText(result.cardName(), "Card name", 500);
        }
    }

    private String catalogCommit() {
        try {
            Process process = new ProcessBuilder("git", "-C", root.toString(), "rev-parse", "HEAD").redirectErrorStream(true).start();
            if (!process.waitFor(5, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return null;
            }
            String output = new String(process.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8).trim();
            return process.exitValue() == 0 && output.matches("[0-9a-f]{40,64}") ? output : null;
        } catch (IOException e) {
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    private static String searchFilter() {
        return "(c.display_name LIKE ? ESCAPE '!' OR t.class_name LIKE ? ESCAPE '!' OR t.set_code LIKE ? ESCAPE '!' OR (t.set_code || ' ' || t.collector_number) LIKE ? ESCAPE '!')";
    }

    private static String pattern(String query) {
        String value = query == null ? "" : query.trim();
        return "%" + value.replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
    }

    private long insertId(String sql, Object... args) {
        return jdbc.queryForObject(sql, Long.class, args);
    }

    private Map<String, Object> one(String sql, Object... args) {
        var rows = jdbc.queryForList(sql, args);
        if (rows.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Review record not found");
        }
        return rows.getFirst();
    }

    private static long number(Object value) {
        return value == null ? 0 : ((Number) value).longValue();
    }

    private static void requireText(String value, String label, int maxLength) {
        require(value != null && !value.isBlank() && value.length() <= maxLength, label + " is required and must be at most " + maxLength + " characters");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }

    public record NewRun(String name, String model, String reasoningEffort) {}
    private record PrintingInsert(long cardId, CardSourceCatalog.Printing printing) {}
    public record Claim(String workerId) {}
    public record Result(String attemptToken, String outcome, String cardName, List<String> findings,
                         String reviewedCommit, String publicationStatus, String publishedCommit,
                         String executionError, String publicationError, Long inputTokens, Long cachedInputTokens,
                         Long outputTokens, BigDecimal estimatedCostUsd) {}
}
