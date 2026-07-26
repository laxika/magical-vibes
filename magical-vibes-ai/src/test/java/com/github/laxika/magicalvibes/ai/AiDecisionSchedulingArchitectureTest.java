package com.github.laxika.magicalvibes.ai;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AiDecisionSchedulingArchitectureTest {

    private static final List<String> SCHEDULING_SOURCES = List.of(
            "AiDecisionScheduler.java",
            "AiDecisionEventSubscriber.java",
            "AiDecisionKind.java");

    @Test
    void decisionSchedulingDoesNotConsumeOutboundNetworkingMessages() throws IOException {
        Path repoRoot = locateRepoRoot();
        Path sourceRoot = repoRoot.resolve(
                "magical-vibes-ai/src/main/java/com/github/laxika/magicalvibes/ai");

        for (String fileName : SCHEDULING_SOURCES) {
            String source = Files.readString(
                    sourceRoot.resolve(fileName), StandardCharsets.UTF_8);
            assertThat(source)
                    .as(fileName + " must schedule only from internal game facts")
                    .doesNotContain("networking.Connection")
                    .doesNotContain("networking.message")
                    .doesNotContain("MessageType")
                    .doesNotContain("sendMessage(");
        }
    }

    @Test
    void liveAndTournamentAiSchedulersRegisterBeforeInitialDecisionFacts() throws IOException {
        Path repoRoot = locateRepoRoot();
        String liveSeating = Files.readString(
                repoRoot.resolve(
                        "magical-vibes-ai/src/main/java/com/github/laxika/magicalvibes/ai/AiPlayerService.java"),
                StandardCharsets.UTF_8);
        String tournamentSeating = Files.readString(
                repoRoot.resolve(
                        "magical-vibes-webservice/src/main/java/com/github/laxika/magicalvibes/webservice/DraftService.java"),
                StandardCharsets.UTF_8);

        assertThat(liveSeating.indexOf("decisionEventSubscriber.register("))
                .isGreaterThanOrEqualTo(0)
                .isLessThan(liveSeating.indexOf("mutationCoordinator.mutate("));
        assertThat(tournamentSeating.indexOf("registerAiForTournamentGame(gameData"))
                .isGreaterThanOrEqualTo(0)
                .isLessThan(tournamentSeating.indexOf("publishTournamentGameOpened(gameData)"));
    }

    private static Path locateRepoRoot() {
        Path directory = Path.of("").toAbsolutePath();
        for (Path candidate = directory; candidate != null; candidate = candidate.getParent()) {
            if (Files.exists(candidate.resolve("settings.gradle.kts"))
                    || Files.exists(candidate.resolve("settings.gradle"))) {
                return candidate;
            }
        }
        throw new IllegalStateException("Could not locate repository root from " + directory);
    }
}
