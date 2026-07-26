package com.github.laxika.magicalvibes.service.interaction;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/** Keeps answer handlers transport-free: projection and delivery belong to the event layer. */
class InteractionHandlerTransportRatchetTest {

    private static final String INTERACTION_SOURCE =
            "magical-vibes-engine/src/main/java/com/github/laxika/magicalvibes/service/interaction";

    @Test
    void interactionHandlersHaveNoDirectSessionDependencyOrOutboundSessionCall() throws IOException {
        Path sourceRoot = locateRepoRoot().resolve(INTERACTION_SOURCE);
        List<String> violations;
        try (Stream<Path> files = Files.walk(sourceRoot)) {
            violations = files
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .flatMap(path -> violations(path, sourceRoot))
                    .sorted()
                    .toList();
        }

        assertThat(violations)
                .as("service/interaction/** must not depend on SessionManager or send outbound sessions")
                .isEmpty();
    }

    private static Stream<String> violations(Path file, Path sourceRoot) {
        try {
            String source = Files.readString(file, StandardCharsets.UTF_8);
            if (source.contains("SessionManager")
                    || source.matches("(?s).*\\.sendToPlayers?\\s*\\(.*")) {
                return Stream.of(sourceRoot.relativize(file).toString().replace('\\', '/'));
            }
            return Stream.empty();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read " + file, exception);
        }
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
