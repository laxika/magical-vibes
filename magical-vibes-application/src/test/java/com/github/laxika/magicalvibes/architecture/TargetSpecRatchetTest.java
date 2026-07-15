package com.github.laxika.magicalvibes.architecture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
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
 * Ratchet for the TargetSpec migration (refactor step 2). It permanently prevents the number of
 * legacy per-effect targeting-method overrides from GROWING while later steps shrink it toward
 * zero: the test fails if any effect record file's legacy-override count exceeds its baseline
 * entry (a record must declare a {@link com.github.laxika.magicalvibes.model.effect.TargetSpec}
 * instead of adding a legacy override), and also fails — asking for a baseline regeneration — if
 * a file drops BELOW its baseline, so the baseline stays honest as records migrate.
 *
 * <p><b>COUNTING RULES ARE DUPLICATED</b> in {@code scripts/targetspec-audit.py} (the step-1
 * audit that generates {@code refactor-docs/targetspec-baseline.txt}, which this test parses).
 * The two implementations MUST be changed in lockstep — if they diverge, the freshly regenerated
 * baseline and this test will disagree and the ratchet becomes noise. The rules, mirrored from
 * that script:
 * <ul>
 *   <li>A record is "in scope" iff it provides its own override of >=1 of the eleven LEGACY
 *       targeting methods on {@code CardEffect} ({@code isPowerToughnessDefining} is NOT one of
 *       them and is kept).</li>
 *   <li>A method is "overridden" by a file when the file contains
 *       {@code public <boolean|int|PermanentPredicate> <name>()} — matched whole-file, regex only,
 *       ignoring the body (constant, conditional, and {@code return false} all count).</li>
 *   <li>The count for a file is the number of DISTINCT legacy methods it overrides; only files
 *       with count >= 1 appear in the baseline.</li>
 * </ul>
 * The scan is line-agnostic (whole-file regex), matching the audit script exactly.
 */
class TargetSpecRatchetTest {

    /** The eleven legacy targeting methods this migration deletes (isPowerToughnessDefining is KEPT). */
    private static final List<String> LEGACY_METHODS = List.of(
            "canTargetPlayer", "canTargetPermanent", "canTargetSpell",
            "canTargetGraveyard", "canTargetAnyGraveyard", "targetsControllersGraveyardOnly",
            "canTargetExile", "targetPredicate", "isSelfTargeting",
            "requiredPlayerTargetCount", "isDamageOrDestruction");

    private static final String EFFECT_PKG =
            "magical-vibes-domain/src/main/java/com/github/laxika/magicalvibes/model/effect";
    private static final String BASELINE = "refactor-docs/targetspec-baseline.txt";

    @Test
    @DisplayName("No effect file exceeds its legacy-targeting-override baseline (and none drops below it silently)")
    void legacyTargetingOverrideCountsMatchBaseline() throws IOException {
        Path repoRoot = locateRepoRoot();
        Path effectPkg = repoRoot.resolve(EFFECT_PKG);

        Map<String, Integer> current = new TreeMap<>();
        Map<String, Set<String>> currentMethods = new TreeMap<>();
        try (Stream<Path> files = Files.list(effectPkg)) {
            for (Path path : (Iterable<Path>) files.sorted()::iterator) {
                if (!fileNameOf(path).endsWith(".java")) {
                    continue;
                }
                Set<String> overridden = overriddenLegacyMethods(readText(path));
                if (!overridden.isEmpty()) {
                    String rel = repoRoot.relativize(path).toString().replace('\\', '/');
                    current.put(rel, overridden.size());
                    currentMethods.put(rel, overridden);
                }
            }
        }

        Map<String, Integer> baseline = loadBaseline(repoRoot.resolve(BASELINE));

        List<String> messages = new ArrayList<>();
        Set<String> allFiles = new TreeSet<>();
        allFiles.addAll(current.keySet());
        allFiles.addAll(baseline.keySet());

        for (String file : allFiles) {
            int now = current.getOrDefault(file, 0);
            Integer wasBoxed = baseline.get(file);
            int was = wasBoxed == null ? 0 : wasBoxed;

            if (now > was) {
                messages.add(String.format(
                        "Legacy targeting overrides grew in %s (was %d, now %d). Overrides present: %s. "
                                + "Do NOT add a canTarget*/isSelfTargeting/isDamageOrDestruction/targetPredicate/"
                                + "requiredPlayerTargetCount override — declare a TargetSpec instead: override "
                                + "targetSpec() to return TargetSpec.harmful(...)/benign(...) for the right "
                                + "TargetCategory, and the legacy booleans derive from it automatically.",
                        file, was, now, String.join(", ", currentMethods.getOrDefault(file, Set.of()))));
            } else if (now < was) {
                if (now == 0 && !Files.exists(repoRoot.resolve(file))) {
                    messages.add(String.format(
                            "Baseline lists %s (=%d) but that file no longer exists. Regenerate the baseline "
                                    + "with python scripts/targetspec-audit.py so the ratchet drops the stale entry.",
                            file, was));
                } else {
                    messages.add(String.format(
                            "Good news: %s dropped from %d to %d legacy overrides. Regenerate the baseline "
                                    + "with python scripts/targetspec-audit.py so the ratchet locks in the improvement.",
                            file, was, now));
                }
            }
        }

        assertThat(messages)
                .withFailMessage(() -> "TargetSpec ratchet failed:\n  " + String.join("\n  ", messages))
                .isEmpty();
    }

    /** Distinct legacy targeting methods the file overrides, per the audit's regex. */
    private static Set<String> overriddenLegacyMethods(String text) {
        Set<String> found = new TreeSet<>();
        for (String method : LEGACY_METHODS) {
            Pattern p = Pattern.compile(
                    "public\\s+(?:boolean|int|PermanentPredicate)\\s+" + Pattern.quote(method) + "\\s*\\(\\s*\\)\\s*\\{");
            if (p.matcher(text).find()) {
                found.add(method);
            }
        }
        return found;
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

    private static Map<String, Integer> loadBaseline(Path baseline) throws IOException {
        Map<String, Integer> result = new TreeMap<>();
        for (String line : Files.readAllLines(baseline, StandardCharsets.UTF_8)) {
            String trimmed = line.strip();
            if (trimmed.isEmpty()) {
                continue;
            }
            int eq = trimmed.lastIndexOf('=');
            if (eq < 0) {
                throw new IllegalStateException("Malformed baseline line (expected path=count): " + line);
            }
            result.put(trimmed.substring(0, eq), Integer.parseInt(trimmed.substring(eq + 1).strip()));
        }
        return result;
    }

    private static String fileNameOf(Path path) {
        Path name = path.getFileName();
        return name == null ? "" : name.toString();
    }

    private static String readText(Path path) {
        try {
            return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
