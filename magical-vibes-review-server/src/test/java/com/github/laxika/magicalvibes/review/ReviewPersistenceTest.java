package com.github.laxika.magicalvibes.review;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.file.Files;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewPersistenceTest {
    @TempDir
    Path root;

    @Test
    void migratesExistingReviewsAndAcceptsLegacyUploadRetries() throws Exception {
        var directory = root.resolve("magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/a");
        Files.createDirectories(directory);
        Files.writeString(directory.resolve("Alpha.java"), "package example; @CardRegistration(set=\"SOS\",collectorNumber=\"1\") public class Alpha extends Card {}");
        long taskId;
        long cardId;
        String payload;
        try (var context = start("classpath:review-db/changelog/001-review-schema.sql")) {
            var service = context.getBean(ReviewService.class);
            // Seed the old schema directly; current run queries require the cost migration.
            var jdbc = context.getBean(JdbcTemplate.class);
            jdbc.update("INSERT INTO review_run(id,name,model,reasoning_effort,created_at) VALUES (1,'Before costs','sol','high',?)", "2026-01-01T00:00:00Z");
            jdbc.update("INSERT INTO review_card(id,class_name,display_name) VALUES (1,'example.Alpha','Alpha')");
            jdbc.update("INSERT INTO review_task(id,run_id,card_id,class_name,source_path,set_code,collector_number) VALUES (1,1,1,'example.Alpha',?,'SOS','1')", directory.resolve("Alpha.java").toString());
            jdbc.update("UPDATE review_settings SET active_run_id=1 WHERE id=1");
            var task = service.claim(new ReviewService.Claim("legacy-worker"));
            taskId = ((Number) task.get("id")).longValue();
            cardId = ((Number) task.get("cardId")).longValue();
            payload = """
                    {"attemptToken":"%s","outcome":"FINDINGS","cardName":"Alpha","findings":["Missing trigger."],
                     "reviewedCommit":"%s","publicationStatus":"NOT_REQUIRED","publishedCommit":null,
                     "executionError":null,"publicationError":null}
                    """.formatted(task.get("attemptToken"), "a".repeat(40));
            jdbc.update("UPDATE review_attempt SET outcome='FINDINGS',publication_status='NOT_REQUIRED',finished_at=?,result_json=? WHERE token=?", "2026-01-01T00:01:00Z", payload, task.get("attemptToken"));
            jdbc.update("INSERT INTO review_finding(attempt_token,description) VALUES (?,?)", task.get("attemptToken"), "Missing trigger.");
            jdbc.update("UPDATE review_task SET status='COMPLETED' WHERE id=?", taskId);
        }
        try (var context = start()) {
            var service = context.getBean(ReviewService.class);
            assertThat(service.task(taskId)).containsEntry("estimatedCostUsd", null).containsEntry("inputTokens", null)
                    .containsEntry("outcome", "FINDINGS");
            assertThat((List<?>) service.card(cardId).get("reviews")).hasSize(1);
            assertThat(service.run(1)).containsEntry("estimatedCostUsd", null).containsEntry("missingCostCount", 1);
            var legacy = context.getBean(ObjectMapper.class).readValue(payload, ReviewService.Result.class);
            service.submit(taskId, legacy);
            assertThat(context.getBean(JdbcTemplate.class).queryForObject("SELECT COUNT(*) FROM review_finding", Integer.class)).isEqualTo(1);
        }
    }

    @Test
    void restartsKeepResultsActiveRunAndUnfinishedClaimsWithoutReassignment() throws Exception {
        var directory = root.resolve("magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/a");
        Files.createDirectories(directory);
        for (String name : List.of("Alpha", "Beta")) {
            Files.writeString(directory.resolve(name + ".java"), "package example; @CardRegistration(set=\"SOS\",collectorNumber=\"1\") public class " + name + " extends Card {}");
        }
        long runId;
        long taskId;
        try (var context = start()) {
            var service = context.getBean(ReviewService.class);
            runId = ((Number) service.createRun(new ReviewService.NewRun("Persistent", "sol", "high")).get("id")).longValue();
            var task = service.claim(new ReviewService.Claim("finished-worker"));
            taskId = ((Number) task.get("id")).longValue();
            service.submit(taskId, new ReviewService.Result((String) task.get("attemptToken"), "FINDINGS", "Alpha", List.of("Missing trigger."), "b".repeat(40), "NOT_REQUIRED", null, null, null, 1000L, 800L, 200L, new BigDecimal("0.004")));
            service.claim(new ReviewService.Claim("disconnected-worker"));
        }
        try (var context = start()) {
            var service = context.getBean(ReviewService.class);
            assertThat(service.run(runId)).containsEntry("active", 1).containsEntry("completed", 1)
                    .containsEntry("running", 1).containsEntry("findingCount", 1);
            assertThat(service.claim(new ReviewService.Claim("replacement"))).isNull();
            assertThat(context.getBean(JdbcTemplate.class).queryForObject("SELECT COUNT(*) FROM DATABASECHANGELOG", Integer.class)).isEqualTo(2);
            assertThat(((Number) service.task(taskId).get("estimatedCostUsd")).doubleValue()).isEqualTo(0.004);
            assertThat(((Number) service.task(taskId).get("inputTokens")).longValue()).isEqualTo(1000);
            service.requeueRun(runId, "RUNNING");
            assertThat(service.claim(new ReviewService.Claim("replacement"))).isNotNull();
        }
    }

    private ConfigurableApplicationContext start() {
        return start("classpath:review-db/changelog/master.yaml");
    }

    private ConfigurableApplicationContext start(String changelog) {
        return new SpringApplicationBuilder(ReviewServerApplication.class).run("--server.port=0",
                "--review.repository-root=" + root, "--review.database-path=" + root.resolve("persistent.sqlite"),
                "--spring.liquibase.change-log=" + changelog);
    }
}
