package com.github.laxika.magicalvibes.review;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.data.Offset.offset;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ReviewServerTest {
    @TempDir
    static Path root;
    @Autowired
    ReviewService service;
    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    CardSourceCatalog catalog;
    @Autowired
    ObjectMapper mapper;
    @Value("${local.server.port}")
    int port;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry properties) {
        properties.add("review.repository-root", () -> root.toString());
        properties.add("review.database-path", () -> root.resolve("reviews.sqlite").toString());
    }

    @BeforeEach
    void reset() throws IOException {
        jdbc.update("UPDATE review_settings SET active_run_id=NULL");
        for (String table : List.of("review_finding", "review_attempt", "review_task", "review_printing", "review_card", "review_run")) {
            jdbc.update("DELETE FROM " + table);
        }
        Path sources = sourceRoot();
        Files.createDirectories(sources);
        try (var files = Files.walk(sources)) {
            for (Path path : files.filter(Files::isRegularFile).toList()) {
                Files.delete(path);
            }
        }
        writeCard("FirstCard", """
                // @CardRegistration(set = "FAKE", collectorNumber = "999")
                @CardRegistration(set = "INR", collectorNumber = "14b")
                @CardRegistration(set = "M10", collectorNumber = "22")
                """);
        writeCard("BackFace", "");
    }

    @Test
    void createsOneTaskPerImplementationAndSnapshotsTheRun() throws Exception {
        var run = newRun("Luna", "luna");
        assertThat(run.get("total")).isEqualTo(1);
        var task = service.claim(new ReviewService.Claim("worker-one"));
        assertThat(task).containsEntry("model", "luna").containsEntry("reasoningEffort", "xhigh")
                .containsEntry("setCode", "INR").containsEntry("collectorNumber", "14b");
        assertThat(service.claim(new ReviewService.Claim("worker-two"))).isNull();
        writeCard("LaterCard", "@CardRegistration(set = \"SOS\", collectorNumber = \"1\")");
        assertThat(service.run(id(run)).get("total")).isEqualTo(1);
        var second = newRun("Sol", "sol");
        assertThat(second.get("total")).isEqualTo(2);
        assertThat(service.cards("M10 22", 0).get("total")).isEqualTo(1L);
        assertThat(service.cards("FAKE", 0).get("total")).isEqualTo(0L);
    }

    @Test
    void assignsTenDifferentCardsUnderConcurrentClaims() throws Exception {
        for (int index = 0; index < 12; index++) {
            writeCard("Card" + index, "@CardRegistration(set = \"SOS\", collectorNumber = \"" + index + "\")");
        }
        newRun("Concurrent", "luna");
        try (var executor = Executors.newFixedThreadPool(10)) {
            var futures = new ArrayList<java.util.concurrent.Future<Map<String, Object>>>();
            for (int index = 0; index < 10; index++) {
                String worker = "worker-" + index;
                futures.add(executor.submit(() -> service.claim(new ReviewService.Claim(worker))));
            }
            var ids = new ArrayList<Object>();
            for (var future : futures) {
                ids.add(future.get().get("id"));
            }
            assertThat(ids).doesNotHaveDuplicates().hasSize(10);
        }
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM review_task WHERE status='RUNNING'", Integer.class)).isEqualTo(10);
    }

    @Test
    void retainsFindingsAcrossModelsAndDoesNotDoubleCountRetries() throws Exception {
        var luna = newRun("Luna baseline", "luna");
        var lunaTask = service.claim(new ReviewService.Claim("one"));
        var lunaResult = result(lunaTask, "FINDINGS", List.of("Incorrect target restriction.", "Missing optional choice."));
        service.submit(id(lunaTask), lunaResult);
        service.submit(id(lunaTask), lunaResult);
        var sol = newRun("Sol baseline", "sol");
        service.activate(id(sol));
        var solTask = service.claim(new ReviewService.Claim("two"));
        service.submit(id(solTask), result(solTask, "FINDINGS", List.of("Incorrect target restriction.")));
        assertThat(service.run(id(luna))).containsEntry("findingCount", 2).containsEntry("cardsWithFindings", 1);
        assertThat(service.run(id(sol))).containsEntry("findingCount", 1);
        var card = service.card(((Number) lunaTask.get("cardId")).longValue());
        assertThat((List<?>) card.get("reviews")).hasSize(2);
        assertThat((List<?>) service.overview().get("models")).hasSize(2);
        service.requeue(id(lunaTask));
        assertThat(service.run(id(luna)).get("findingCount")).isEqualTo(0);
        service.activate(id(luna));
        var retry = service.claim(new ReviewService.Claim("three"));
        assertThatThrownBy(() -> service.submit(id(lunaTask), lunaResult)).isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("409");
        service.submit(id(retry), result(retry, "PASS", List.of()));
        assertThat((List<?>) service.task(id(retry)).get("attempts")).hasSize(2);
        assertThat(service.run(id(luna))).containsEntry("findingCount", 0).containsEntry("completed", 1);
        assertThat(service.cards("First Card", 0).get("total")).isEqualTo(1L);
    }

    @Test
    void switchingAndPausingRunsDoNotInvalidateExistingClaims() throws Exception {
        var first = newRun("First", "luna");
        var task = service.claim(new ReviewService.Claim("one"));
        var second = newRun("Second", "sol");
        service.activate(id(second));
        assertThat(service.claim(new ReviewService.Claim("two")).get("runId")).isEqualTo(second.get("id"));
        service.activate(null);
        newRun("Created while paused", "luna");
        assertThat(service.claim(new ReviewService.Claim("three"))).isNull();
        service.submit(id(task), result(task, "PASS", List.of()));
        assertThat(service.run(id(first)).get("completed")).isEqualTo(1);
        assertThat(service.requeueRun(id(second), "RUNNING")).isEqualTo(1);
    }

    @Test
    void executionAndPublicationFailuresAreSeparateFromFindingCounts() throws Exception {
        var run = newRun("Failures", "luna");
        var task = service.claim(new ReviewService.Claim("one"));
        service.submit(id(task), new ReviewService.Result((String) task.get("attemptToken"), "ERROR", null, List.of(),
                null, "NOT_REQUIRED", null, "Codex could not finish", null, null, null, null, null));
        assertThat(service.run(id(run))).containsEntry("failed", 1).containsEntry("findingCount", 0);
        service.requeueRun(id(run), "FAILED");
        var retry = service.claim(new ReviewService.Claim("two"));
        service.submit(id(retry), new ReviewService.Result((String) retry.get("attemptToken"), "FINDINGS", "First Card", List.of("Missing trigger."),
                "a".repeat(40), "FAILED", null, null, "Push rejected", null, null, null, null));
        assertThat(service.run(id(run))).containsEntry("completed", 1).containsEntry("failed", 0)
                .containsEntry("findingCount", 1).containsEntry("publicationFailures", 1);
    }

    @Test
    void requeuesEveryUnfinishedTaskWithoutTouchingCompletedOrQueuedOnes() throws Exception {
        for (int index = 2; index <= 4; index++) {
            writeCard("Card" + index, "@CardRegistration(set = \"SOS\", collectorNumber = \"" + index + "\")");
        }
        var run = newRun("Unfinished", "luna");
        var completed = service.claim(new ReviewService.Claim("one"));
        service.submit(id(completed), result(completed, "FINDINGS", List.of("Missing trigger.")));
        var failed = service.claim(new ReviewService.Claim("two"));
        service.submit(id(failed), new ReviewService.Result((String) failed.get("attemptToken"), "ERROR", null, List.of(),
                null, "NOT_REQUIRED", null, "Codex could not finish", null, null, null, null, null));
        var running = service.claim(new ReviewService.Claim("three"));
        assertThat(service.run(id(run))).containsEntry("completed", 1).containsEntry("failed", 1)
                .containsEntry("running", 1).containsEntry("created", 1);

        assertThat(service.requeueRun(id(run), "UNFINISHED")).isEqualTo(2);

        assertThat(service.run(id(run))).containsEntry("completed", 1).containsEntry("failed", 0)
                .containsEntry("running", 0).containsEntry("created", 3).containsEntry("findingCount", 1);
        assertThatThrownBy(() -> service.submit(id(running), result(running, "PASS", List.of())))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("409");
        assertThatThrownBy(() -> service.requeueRun(id(run), "COMPLETED"))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("400");
    }

    @Test
    void validatesHttpContractsAndServesTheDashboard() throws Exception {
        var client = HttpClient.newHttpClient();
        var home = client.send(HttpRequest.newBuilder(uri("/")).GET().build(), HttpResponse.BodyHandlers.ofString());
        assertThat(home.statusCode()).isEqualTo(200);
        assertThat(home.body()).contains("Card review desk", "new-run");
        assertThat(post(client, "/api/runs", "{}").statusCode()).isEqualTo(400);
        var created = post(client, "/api/runs", mapper.writeValueAsString(new ReviewService.NewRun("HTTP", "luna", "high")));
        assertThat(created.statusCode()).isEqualTo(201);
        var claim = post(client, "/api/tasks/claim", "{\"workerId\":\"http-worker\"}");
        assertThat(claim.statusCode()).isEqualTo(200);
        assertThat(mapper.readTree(claim.body()).path("reasoningEffort").asText()).isEqualTo("high");
        assertThat(post(client, "/api/tasks/claim", "{\"workerId\":\"http-worker\"}").statusCode()).isEqualTo(204);
        assertThat(client.send(HttpRequest.newBuilder(uri("/api/cards?query=%25")).GET().build(), HttpResponse.BodyHandlers.ofString()).body())
                .contains("\"total\" : 0");
    }

    @Test
    void totalsCostsAcrossCardsAndPreviousAttemptsWithoutChangingRunCounts() throws Exception {
        writeCard("SecondCard", "@CardRegistration(set = \"SOS\", collectorNumber = \"2\")");
        var run = newRun("Cost totals", "luna");
        assertThat(run).containsEntry("estimatedCostUsd", null).containsEntry("missingCostCount", 0).containsEntry("pricedCards", 0);
        var first = service.claim(new ReviewService.Claim("one"));
        service.submit(id(first), new ReviewService.Result((String) first.get("attemptToken"), "FINDINGS", "First Card",
                List.of("Missing trigger.", "Wrong target."), "a".repeat(40), "NOT_REQUIRED", null, null, null,
                1000L, 0L, 200L, new BigDecimal("0.12")));
        var second = service.claim(new ReviewService.Claim("two"));
        service.submit(id(second), new ReviewService.Result((String) second.get("attemptToken"), "ERROR", null, List.of(),
                null, "NOT_REQUIRED", null, "Review failed", null, 1000L, 0L, 200L, new BigDecimal("0.34")));
        assertThat(((Number) service.run(id(run)).get("estimatedCostUsd")).doubleValue()).isCloseTo(0.46, offset(0.000001));
        assertThat(service.run(id(run))).containsEntry("total", 2).containsEntry("completed", 1)
                .containsEntry("failed", 1).containsEntry("findingCount", 2).containsEntry("missingCostCount", 0)
                .containsEntry("pricedCards", 2);

        service.requeue(id(first));
        assertThat(((Number) service.run(id(run)).get("estimatedCostUsd")).doubleValue()).isCloseTo(0.46, offset(0.000001));
        var retry = service.claim(new ReviewService.Claim("retry"));
        service.submit(id(retry), result(retry, "PASS", List.of()));
        assertThat(service.run(id(run))).containsEntry("missingCostCount", 1).containsEntry("findingCount", 0)
                .containsEntry("pricedCards", 2);

        var otherRun = newRun("Zero cost", "sol");
        service.activate(id(otherRun));
        var free = service.claim(new ReviewService.Claim("free"));
        service.submit(id(free), new ReviewService.Result((String) free.get("attemptToken"), "PASS", "First Card", List.of(),
                "a".repeat(40), "NOT_REQUIRED", null, null, null, 0L, 0L, 0L, BigDecimal.ZERO));
        assertThat(((Number) service.run(id(otherRun)).get("estimatedCostUsd")).doubleValue()).isZero();
        var runs = (List<?>) service.overview().get("runs");
        assertThat(((Number) ((Map<?, ?>) runs.get(1)).get("estimatedCostUsd")).doubleValue()).isCloseTo(0.46, offset(0.000001));
        assertThat(((Map<?, ?>) runs.get(1)).get("missingCostCount")).isEqualTo(1);
    }

    @Test
    void storesCostThroughHttpAndRetainsItInCardAttemptHistory() throws Exception {
        var run = newRun("Costs", "gpt-5.6-luna");
        var task = service.claim(new ReviewService.Claim("cost-worker"));
        var result = new ReviewService.Result((String) task.get("attemptToken"), "PASS", "First Card", List.of(),
                "a".repeat(40), "NOT_REQUIRED", null, null, null, 1000L, 800L, 200L, new BigDecimal("0.000296"));
        var client = HttpClient.newHttpClient();
        String body = mapper.writeValueAsString(result);
        assertThat(post(client, "/api/tasks/" + id(task) + "/result", body).statusCode()).isEqualTo(204);
        assertThat(post(client, "/api/tasks/" + id(task) + "/result", body).statusCode()).isEqualTo(204);
        var overview = client.send(HttpRequest.newBuilder(uri("/api/overview")).GET().build(), HttpResponse.BodyHandlers.ofString());
        assertThat(mapper.readTree(overview.body()).path("runs").get(0).path("estimatedCostUsd").asDouble()).isEqualTo(0.000296);
        assertThat(service.task(id(task))).containsEntry("inputTokens", 1000).containsEntry("cachedInputTokens", 800)
                .containsEntry("outputTokens", 200);
        assertThat(((Number) service.task(id(task)).get("estimatedCostUsd")).doubleValue()).isEqualTo(0.000296);
        var tasks = (List<?>) service.tasks(id(run), "", "", 0).get("items");
        assertThat(((Map<?, ?>) tasks.getFirst()).get("estimatedCostUsd")).isNotNull();
        service.requeue(id(task));
        var retry = service.claim(new ReviewService.Claim("retry-worker"));
        service.submit(id(retry), result(retry, "PASS", List.of()));
        var cardResponse = client.send(HttpRequest.newBuilder(uri("/api/cards/" + task.get("cardId"))).GET().build(), HttpResponse.BodyHandlers.ofString());
        var attempts = mapper.readTree(cardResponse.body()).path("reviews").get(0).path("attempts");
        assertThat(attempts.size()).isEqualTo(2);
        assertThat(attempts.get(0).path("estimatedCostUsd").isNull()).isTrue();
        assertThat(attempts.get(1).path("estimatedCostUsd").asDouble()).isEqualTo(0.000296);
        assertThat(attempts.get(1).path("cachedInputTokens").asLong()).isEqualTo(800);
        var runResponse = client.send(HttpRequest.newBuilder(uri("/api/runs/" + id(run))).GET().build(), HttpResponse.BodyHandlers.ofString());
        assertThat(mapper.readTree(runResponse.body()).path("estimatedCostUsd").asDouble()).isEqualTo(0.000296);
        assertThat(mapper.readTree(runResponse.body()).path("missingCostCount").asInt()).isEqualTo(1);
    }

    @Test
    void rejectsInvalidCostAndUsageAndAcceptsUsageWithoutPricing() throws Exception {
        var run = newRun("Cost validation", "custom-model");
        var task = service.claim(new ReviewService.Claim("worker"));
        for (var counts : List.of(new Long[]{-1L, 0L, 0L}, new Long[]{1L, 2L, 0L}, new Long[]{1L, null, 0L})) {
            var invalid = new ReviewService.Result((String) task.get("attemptToken"), "PASS", "First Card", List.of(),
                    "a".repeat(40), "NOT_REQUIRED", null, null, null, counts[0], counts[1], counts[2], null);
            assertThatThrownBy(() -> service.submit(id(task), invalid)).isInstanceOf(ResponseStatusException.class).hasMessageContaining("400");
        }
        for (var cost : List.of(new BigDecimal("-0.01"), new BigDecimal("1e400"))) {
            var invalid = new ReviewService.Result((String) task.get("attemptToken"), "PASS", "First Card", List.of(),
                    "a".repeat(40), "NOT_REQUIRED", null, null, null, 1L, 0L, 0L, cost);
            assertThatThrownBy(() -> service.submit(id(task), invalid)).isInstanceOf(ResponseStatusException.class).hasMessageContaining("400");
        }
        var noUsage = new ReviewService.Result((String) task.get("attemptToken"), "PASS", "First Card", List.of(),
                "a".repeat(40), "NOT_REQUIRED", null, null, null, null, null, null, BigDecimal.ZERO);
        assertThatThrownBy(() -> service.submit(id(task), noUsage)).isInstanceOf(ResponseStatusException.class).hasMessageContaining("400");
        service.submit(id(task), new ReviewService.Result((String) task.get("attemptToken"), "ERROR", null, List.of(),
                null, "NOT_REQUIRED", null, "Review failed", null, 1000L, 500L, 100L, null));
        assertThat(service.task(id(task))).containsEntry("estimatedCostUsd", null).containsEntry("inputTokens", 1000);
        assertThat(service.run(id(run))).containsEntry("estimatedCostUsd", null).containsEntry("missingCostCount", 1);
    }

    @Test
    void rejectsContradictoryAndConflictingResultsWithoutChangingCounts() throws Exception {
        newRun("Validation", "luna");
        var task = service.claim(new ReviewService.Claim("one"));
        assertThatThrownBy(() -> service.submit(id(task), result(task, "PASS", List.of("Bug"))))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("400");
        service.submit(id(task), result(task, "PASS", List.of()));
        assertThatThrownBy(() -> service.submit(id(task), result(task, "FINDINGS", List.of("Bug"))))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("409");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM review_finding", Integer.class)).isZero();
    }

    private Map<String, Object> newRun(String name, String model) throws IOException {
        return service.createRun(new ReviewService.NewRun(name, model, "xhigh"));
    }

    private ReviewService.Result result(Map<String, Object> task, String outcome, List<String> findings) {
        return new ReviewService.Result((String) task.get("attemptToken"), outcome, "First Card", findings,
                "a".repeat(40), "NOT_REQUIRED", null, null, null, null, null, null, null);
    }

    private Path sourceRoot() {
        return root.resolve("magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/a");
    }

    private void writeCard(String name, String registrations) throws IOException {
        Files.writeString(sourceRoot().resolve(name + ".java"), "package com.github.laxika.magicalvibes.cards.a;\n"
                + registrations + "\npublic class " + name + " extends Card {}\n");
    }

    private long id(Map<String, Object> value) { return ((Number) value.get("id")).longValue(); }
    private URI uri(String path) { return URI.create("http://localhost:" + port + path); }
    private HttpResponse<String> post(HttpClient client, String path, String body) throws Exception {
        return client.send(HttpRequest.newBuilder(uri(path)).header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }
}
