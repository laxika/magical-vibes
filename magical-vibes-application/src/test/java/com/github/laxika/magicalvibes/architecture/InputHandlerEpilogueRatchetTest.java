package com.github.laxika.magicalvibes.architecture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
 * Ratchet against hand-rolled completion epilogues in the input handler services.
 *
 * <p>Every input-completion handler that finishes a choice begun mid-resolution must end via an
 * {@code InputCompletionService} epilogue ({@code processMayAbilitiesThenAutoPass} /
 * {@code sbaProcessMayAbilitiesThenAutoPass}): those resume the stack entry parked in
 * {@code GameData.pendingEffectResolutionEntry}. A handler that instead calls
 * {@code turnProgressionService.resolveAutoPass(...)} directly can leave the parked entry
 * dangling — the spell's remaining effects are silently dropped and
 * {@code GameData.deferPlayerLossCheck} stays wedged (the Fleshbag Marauder bug class).
 *
 * <p>This test counts direct {@code resolveAutoPass(} call sites per file under
 * {@code service/input/} (excluding {@code InputCompletionService}, the sanctioned owner) and
 * fails when any file's count exceeds its baseline — or silently drops below it, so the baseline
 * stays honest as remaining hand-rolled tails are converted.
 */
class InputHandlerEpilogueRatchetTest {

    private static final String INPUT_PKG =
            "magical-vibes-engine/src/main/java/com/github/laxika/magicalvibes/service/input";

    /** The one file allowed to call resolveAutoPass freely: it IS the shared epilogue. */
    private static final Set<String> SANCTIONED_FILES = Set.of("InputCompletionService.java");

    private static final Pattern RESOLVE_AUTO_PASS_RE = Pattern.compile("\\bresolveAutoPass\\s*\\(");

    /**
     * Direct resolveAutoPass call sites per file as of the 2026-07 epilogue unification.
     * Counts may only go DOWN (convert the tail to an InputCompletionService epilogue and
     * lower the entry) — never up.
     */
    private static final Map<String, Integer> BASELINE = Map.of(
            "CardChoiceHandlerService.java", 7,
            "ChoiceHandlerService.java", 19,
            "GraveyardChoiceHandlerService.java", 2,
            "LibraryChoiceHandlerService.java", 1,
            "MayCopyHandlerService.java", 2,
            "MayMiscHandlerService.java", 6,
            "MultiPermanentChoiceHandlerService.java", 17,
            "PermanentChoiceBattlefieldHandlerService.java", 1,
            "PermanentChoiceSpellHandlerService.java", 5,
            "PermanentChoiceTriggerHandlerService.java", 29);

    @Test
    @DisplayName("No input handler file gains a hand-rolled resolveAutoPass epilogue")
    void resolveAutoPassCountsMatchBaseline() throws IOException {
        Path inputPkg = locateRepoRoot().resolve(INPUT_PKG);
        assertThat(inputPkg).isDirectory();

        Map<String, Integer> current = new TreeMap<>();
        try (Stream<Path> files = Files.list(inputPkg)) {
            for (Path path : (Iterable<Path>) files.sorted()::iterator) {
                String name = path.getFileName().toString();
                if (!name.endsWith(".java") || SANCTIONED_FILES.contains(name)) {
                    continue;
                }
                Matcher m = RESOLVE_AUTO_PASS_RE.matcher(
                        new String(Files.readAllBytes(path), StandardCharsets.UTF_8));
                int count = 0;
                while (m.find()) {
                    count++;
                }
                if (count > 0) {
                    current.put(name, count);
                }
            }
        }

        List<String> messages = new java.util.ArrayList<>();
        Set<String> allFiles = new TreeSet<>();
        allFiles.addAll(current.keySet());
        allFiles.addAll(BASELINE.keySet());

        for (String file : allFiles) {
            int now = current.getOrDefault(file, 0);
            int was = BASELINE.getOrDefault(file, 0);
            if (now > was) {
                messages.add(String.format(
                        "%s gained a direct resolveAutoPass call (was %d, now %d). Do NOT hand-roll a "
                                + "completion epilogue: end the handler with InputCompletionService."
                                + "processMayAbilitiesThenAutoPass / sbaProcessMayAbilitiesThenAutoPass, "
                                + "which resume the stack entry parked in GameData.pendingEffectResolutionEntry. "
                                + "A bare resolveAutoPass tail leaves the parked entry dangling: the spell's "
                                + "remaining effects are silently dropped and deferPlayerLossCheck stays wedged.",
                        file, was, now));
            } else if (now < was) {
                messages.add(String.format(
                        "Good news: %s dropped from %d to %d direct resolveAutoPass calls. Lower its entry "
                                + "in this test's BASELINE so the ratchet locks in the improvement.",
                        file, was, now));
            }
        }

        assertThat(messages)
                .withFailMessage(() -> "Input-handler epilogue ratchet failed:\n  " + String.join("\n  ", messages))
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
