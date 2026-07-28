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
 * <p>This test counts direct {@code resolveAutoPass(} call sites per file across every package that
 * answers player input — {@code service/input/} and {@code service/interaction/}, plus the
 * input-answer dispatchers that live elsewhere ({@link #EXTRA_FILES}) — excluding
 * {@code InputCompletionService}, the sanctioned owner. It fails when any file's count exceeds its
 * baseline, or silently drops below it, so the baseline stays honest as remaining hand-rolled tails
 * are converted.
 *
 * <p>The scan originally covered {@code service/input/} alone, which is exactly why the dangling
 * parks in {@code ExileSupport} (Mirror of Fate) and {@code PermanentAuctionService} (Thieves'
 * Auction) went unnoticed: both answer player input from outside that package.
 */
class InputHandlerEpilogueRatchetTest {

    private static final String ENGINE_SRC =
            "magical-vibes-engine/src/main/java/com/github/laxika/magicalvibes";

    /** Package trees whose every file answers player input. */
    private static final List<String> SCANNED_PKGS = List.of(
            ENGINE_SRC + "/service/input",
            ENGINE_SRC + "/service/interaction");

    /**
     * Input-answer dispatchers outside the scanned packages. Add a file here when it grows a
     * {@code handle...Choice}/{@code applyPick}-style entry point reached from player input.
     */
    private static final List<String> EXTRA_FILES = List.of(
            ENGINE_SRC + "/service/PermanentAuctionService.java",
            ENGINE_SRC + "/service/effect/normalfx/ExileSupport.java");

    /** The one file allowed to call resolveAutoPass freely: it IS the shared epilogue. */
    private static final Set<String> SANCTIONED_FILES = Set.of("InputCompletionService.java");
    private static final Pattern RESOLVE_AUTO_PASS_RE = Pattern.compile("\\bresolveAutoPass\\s*\\(");
    private static final Pattern INVALIDATE_ALL_PLAYER_VIEWS_RE =
            Pattern.compile("\\binvalidateAllPlayerViews\\s*\\(");

    /**
     * Direct resolveAutoPass call sites per file as of the 2026-07 epilogue unification.
     * Counts may only go DOWN (convert the tail to an InputCompletionService epilogue and
     * lower the entry) — never up.
     */
    private static final Map<String, Integer> BASELINE = Map.of();

    @Test
    @DisplayName("No input handler file gains a hand-rolled resolveAutoPass epilogue")
    void resolveAutoPassCountsMatchBaseline() throws IOException {
        Map<String, Integer> current = new TreeMap<>();
        for (Path path : scannedFiles()) {
            Matcher m = RESOLVE_AUTO_PASS_RE.matcher(Files.readString(path, StandardCharsets.UTF_8));
            int count = 0;
            while (m.find()) {
                count++;
            }
            if (count > 0) {
                current.put(path.getFileName().toString(), count);
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

    @Test
    @DisplayName("Generic input handlers publish state only through InputCompletionService")
    void genericInputInvalidationsUseTheSharedCompletionEpilogue() throws IOException {
        Path inputPkg = locateRepoRoot().resolve(ENGINE_SRC + "/service/input");
        List<String> failures = new java.util.ArrayList<>();

        try (Stream<Path> files = Files.list(inputPkg)) {
            for (Path path : (Iterable<Path>) files.filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".java"))
                    .filter(file -> !SANCTIONED_FILES.contains(file.getFileName().toString()))
                    .sorted()::iterator) {
                String source = Files.readString(path, StandardCharsets.UTF_8);
                Matcher matcher = INVALIDATE_ALL_PLAYER_VIEWS_RE.matcher(source);
                if (matcher.find()) {
                    failures.add(path.getFileName()
                            + " records state directly instead of using InputCompletionService");
                }
            }
        }

        assertThat(failures)
                .withFailMessage(() -> "Generic input completion ownership regressed:\n  "
                        + String.join("\n  ", failures))
                .isEmpty();
    }

    /** Every scanned source file: the input-answering package trees plus {@link #EXTRA_FILES}. */
    private static List<Path> scannedFiles() throws IOException {
        Path root = locateRepoRoot();
        List<Path> paths = new java.util.ArrayList<>();
        for (String pkg : SCANNED_PKGS) {
            Path dir = root.resolve(pkg);
            assertThat(dir).isDirectory();
            try (Stream<Path> files = Files.walk(dir)) {
                files.filter(Files::isRegularFile)
                        .filter(file -> file.toString().endsWith(".java"))
                        .filter(file -> !SANCTIONED_FILES.contains(file.getFileName().toString()))
                        .sorted()
                        .forEach(paths::add);
            }
        }
        for (String extra : EXTRA_FILES) {
            Path file = root.resolve(extra);
            assertThat(file).isRegularFile();
            paths.add(file);
        }
        return paths;
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
