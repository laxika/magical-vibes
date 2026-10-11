package com.github.laxika.magicalvibes.architecture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Ratchet against damage multiplied before it reaches the replacement pool.
 *
 * <p>Damage multipliers and "deals that much damage plus N" bonuses are replacement effects the
 * damaged player, or the damaged permanent's controller, orders against every other replacement
 * and prevention effect (CR 616.1). The damage pool ({@code service/effect/replacement/damage/})
 * offers each of them as an option, so an effect that deals damage passes the unmodified amount to
 * {@code DamageSupport}. Calling {@code GameQueryService.applyDamageMultiplier} or
 * {@code applyCombatDamageMultiplier} first would apply them in a fixed order, and a second time
 * in the pool.
 *
 * <p>This test counts those call sites per engine source file, excluding {@code GameQueryService},
 * which defines them as predictions. It fails when a file's count rises above its baseline, or
 * drops below it, so the baseline stays honest.
 */
class DamageMultiplierRatchetTest {

    private static final String ENGINE_SRC =
            "magical-vibes-engine/src/main/java/com/github/laxika/magicalvibes";

    private static final Set<String> SANCTIONED_FILES = Set.of("GameQueryService.java");
    private static final Pattern MULTIPLIER_CALL_RE =
            Pattern.compile("\\bapply(?:Combat)?DamageMultiplier\\s*\\(");

    /** Call sites per file. Counts may only go down. */
    private static final Map<String, Integer> BASELINE = Map.of();

    @Test
    @DisplayName("No engine file multiplies damage before the replacement pool")
    void multiplierCallCountsMatchBaseline() throws IOException {
        Map<String, Integer> current = new TreeMap<>();
        Path root = locateRepoRoot().resolve(ENGINE_SRC);
        try (Stream<Path> files = Files.walk(root)) {
            for (Path path : (Iterable<Path>) files.filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".java"))
                    .filter(file -> !SANCTIONED_FILES.contains(file.getFileName().toString()))
                    .sorted()::iterator) {
                Matcher matcher = MULTIPLIER_CALL_RE.matcher(Files.readString(path, StandardCharsets.UTF_8));
                int count = 0;
                while (matcher.find()) {
                    count++;
                }
                if (count > 0) {
                    current.put(path.getFileName().toString(), count);
                }
            }
        }

        List<String> messages = new ArrayList<>();
        Set<String> allFiles = new TreeSet<>(current.keySet());
        allFiles.addAll(BASELINE.keySet());
        for (String file : allFiles) {
            int now = current.getOrDefault(file, 0);
            int was = BASELINE.getOrDefault(file, 0);
            if (now > was) {
                messages.add(String.format(
                        "%s gained a damage multiplier call (was %d, now %d). Pass the unmodified amount to "
                                + "DamageSupport: the damage pool offers every multiplier and bonus as a "
                                + "replacement effect its recipient orders (CR 616.1).",
                        file, was, now));
            } else if (now < was) {
                messages.add(String.format(
                        "%s dropped from %d to %d damage multiplier calls. Lower its entry in this test's "
                                + "BASELINE.", file, was, now));
            }
        }

        assertThat(messages)
                .withFailMessage(() -> "Damage multiplier ratchet failed:\n  " + String.join("\n  ", messages))
                .isEmpty();
    }

    /** Walk up from the test working directory until a Gradle settings file is found. */
    private static Path locateRepoRoot() {
        Path dir = Path.of("").toAbsolutePath();
        for (Path p = dir; p != null; p = p.getParent()) {
            if (Files.exists(p.resolve("settings.gradle.kts")) || Files.exists(p.resolve("settings.gradle"))) {
                return p;
            }
        }
        throw new IllegalStateException(
                "Could not locate repo root (no settings.gradle[.kts]) walking up from " + dir);
    }
}
