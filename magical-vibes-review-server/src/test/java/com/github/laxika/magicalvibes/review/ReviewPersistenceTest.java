package com.github.laxika.magicalvibes.review;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewPersistenceTest {
    @TempDir
    Path root;

    @Test
    void restartsKeepResultsActiveRunAndUnfinishedClaimsWithoutReassignment() throws Exception {
        var directory = root.resolve("magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/a");
        Files.createDirectories(directory);
        for (String name : List.of("Alpha", "Beta")) {
            Files.writeString(directory.resolve(name + ".java"), "package example; @CardRegistration(set=\"SOS\",collectorNumber=\"1\") public class " + name + " extends Card {}");
        }
        long runId;
        try (var context = start()) {
            var service = context.getBean(ReviewService.class);
            runId = ((Number) service.createRun(new ReviewService.NewRun("Persistent", "sol", "high")).get("id")).longValue();
            var task = service.claim(new ReviewService.Claim("finished-worker"));
            service.submit(((Number) task.get("id")).longValue(), new ReviewService.Result((String) task.get("attemptToken"), "FINDINGS", "Alpha", List.of("Missing trigger."), "b".repeat(40), "NOT_REQUIRED", null, null, null));
            service.claim(new ReviewService.Claim("disconnected-worker"));
        }
        try (var context = start()) {
            var service = context.getBean(ReviewService.class);
            assertThat(service.run(runId)).containsEntry("active", 1).containsEntry("completed", 1)
                    .containsEntry("running", 1).containsEntry("findingCount", 1);
            assertThat(service.claim(new ReviewService.Claim("replacement"))).isNull();
            assertThat(context.getBean(JdbcTemplate.class).queryForObject("SELECT COUNT(*) FROM DATABASECHANGELOG", Integer.class)).isEqualTo(1);
            service.requeueRun(runId, "RUNNING");
            assertThat(service.claim(new ReviewService.Claim("replacement"))).isNotNull();
        }
    }

    private ConfigurableApplicationContext start() {
        return new SpringApplicationBuilder(ReviewServerApplication.class).run("--server.port=0",
                "--review.repository-root=" + root, "--review.database-path=" + root.resolve("persistent.sqlite"));
    }
}
