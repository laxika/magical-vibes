package com.github.laxika.magicalvibes.architecture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
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
 * Ratchet for the effect-system refactor (step 2). It permanently prevents the
 * {@code instanceof <ConcreteEffectType>} coupling problem from GROWING while later
 * steps shrink it: the test fails if any engine/AI file's effect-instanceof count
 * exceeds its baseline entry (or a non-baseline file gains any), and also fails —
 * with a "please lower the baseline" message — if a file drops BELOW its baseline,
 * so the baseline stays honest as refactoring proceeds.
 *
 * <p><b>COUNTING RULES ARE DUPLICATED</b> in {@code scripts/effect-coupling-audit.py}
 * (the step-1 audit that generates the baseline this test reads). The two implementations
 * MUST be changed in lockstep — if they diverge, the freshly regenerated baseline and this
 * test will disagree and the ratchet becomes noise. The rules, mirrored from that script:
 * <ul>
 *   <li>Effect-type universe = every top-level type declared under
 *       {@code magical-vibes-domain/.../model/effect/} (file name minus {@code .java}).</li>
 *   <li>An {@code instanceof X} is a VIOLATION iff: X is an effect type, X is NOT a
 *       structural wrapper (see {@link #STRUCTURAL_WRAPPERS}), X is NOT a capability/marker
 *       INTERFACE (the effect file declares {@code interface}, not record/class/enum), and
 *       the consuming file is NOT under {@code service/effect/**} or {@code service/validate/**}.</li>
 *   <li>Matching is word-boundary; the final identifier segment of a qualified
 *       {@code instanceof pkg.X} is used.</li>
 * </ul>
 * The scan is line-agnostic (whole-file regex), so an {@code instanceof} mentioned inside a
 * comment or string literal is counted. That is acceptable for a ratchet: false positives only
 * tighten it, and the same is true of the audit script, so the two stay consistent.
 */
class EffectDispatchRatchetTest {

    /** Structural wrapper types any code may legitimately unwrap; exempt from violation counting. */
    private static final Set<String> STRUCTURAL_WRAPPERS = Set.of(
            "ConditionalEffect",
            "ConditionalReplacementEffect",
            "MayEffect",
            "MayPayManaEffect",
            "MayPayTapPermanentsEffect",
            "ChooseOneEffect",
            "CostEffect",
            "CardEffect");

    private static final Pattern INSTANCEOF_RE = Pattern.compile("\\binstanceof\\s+([A-Za-z_][\\w.]*)");
    private static final Pattern KIND_RE = Pattern.compile("\\b(interface|record|enum|class)\\s+([A-Za-z_]\\w*)");

    private static final String EFFECT_PKG =
            "magical-vibes-domain/src/main/java/com/github/laxika/magicalvibes/model/effect";
    private static final String ENGINE_SRC = "magical-vibes-engine/src/main/java";
    private static final String AI_SRC = "magical-vibes-ai/src/main/java";
    private static final String BASELINE = "refactor-docs/effect-dispatch-baseline.txt";

    @Test
    @DisplayName("No file exceeds its effect-instanceof baseline (and none drops below it silently)")
    void effectInstanceofCountsMatchBaseline() throws IOException {
        Path repoRoot = locateRepoRoot();

        EffectTypes types = loadEffectTypes(repoRoot.resolve(EFFECT_PKG));
        Map<String, Integer> current = new TreeMap<>();
        current.putAll(scanSources(repoRoot, repoRoot.resolve(ENGINE_SRC), types));
        scanSources(repoRoot, repoRoot.resolve(AI_SRC), types)
                .forEach((f, c) -> current.merge(f, c, Integer::sum));

        Map<String, Integer> baseline = loadBaseline(repoRoot.resolve(BASELINE));

        List<String> messages = new java.util.ArrayList<>();
        Set<String> allFiles = new TreeSet<>();
        allFiles.addAll(current.keySet());
        allFiles.addAll(baseline.keySet());

        for (String file : allFiles) {
            int now = current.getOrDefault(file, 0);
            Integer wasBoxed = baseline.get(file);
            int was = wasBoxed == null ? 0 : wasBoxed;

            if (now > was) {
                messages.add(String.format(
                        "New instanceof-on-CardEffect dispatch added in %s (was %d, now %d). "
                                + "Register knowledge with the effect's handler/validator instead, or — only if this "
                                + "is genuinely sanctioned — regenerate the baseline with: "
                                + "python scripts/effect-coupling-audit.py",
                        file, was, now));
            } else if (now < was) {
                if (now == 0 && !Files.exists(repoRoot.resolve(file))) {
                    messages.add(String.format(
                            "Baseline lists %s (=%d) but that file no longer exists. Regenerate the baseline "
                                    + "(python scripts/effect-coupling-audit.py) so the ratchet drops the stale entry.",
                            file, was));
                } else {
                    messages.add(String.format(
                            "Good news: %s dropped from %d to %d. Regenerate the baseline "
                                    + "(python scripts/effect-coupling-audit.py) so the ratchet locks in the improvement.",
                            file, was, now));
                }
            }
        }

        assertThat(messages)
                .withFailMessage(() -> "Effect-dispatch ratchet failed:\n  " + String.join("\n  ", messages))
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

    private record EffectTypes(Set<String> all, Set<String> wrapperOrInterface) {
    }

    /** Load the effect-type universe and the set of names exempt as wrappers or interfaces. */
    private static EffectTypes loadEffectTypes(Path effectPkg) throws IOException {
        Set<String> all = new TreeSet<>();
        Set<String> wrapperOrInterface = new TreeSet<>(STRUCTURAL_WRAPPERS);
        try (Stream<Path> files = Files.list(effectPkg)) {
            for (Path path : (Iterable<Path>) files.sorted()::iterator) {
                String name = fileNameOf(path);
                if (!name.endsWith(".java")) {
                    continue;
                }
                String type = name.substring(0, name.length() - ".java".length());
                all.add(type);
                if ("interface".equals(declaredKind(readText(path), type))) {
                    wrapperOrInterface.add(type);
                }
            }
        }
        return new EffectTypes(all, wrapperOrInterface);
    }

    /**
     * Classify the file's public type: the keyword of the first {@code interface|record|enum|class}
     * declaration whose name matches the file, else the first such keyword, else {@code record}.
     */
    private static String declaredKind(String text, String typeName) {
        Matcher m = KIND_RE.matcher(text);
        String firstKind = null;
        while (m.find()) {
            if (firstKind == null) {
                firstKind = m.group(1);
            }
            if (m.group(2).equals(typeName)) {
                return m.group(1);
            }
        }
        return firstKind == null ? "record" : firstKind;
    }

    /** Recursively scan a source root, returning per-file violation totals (only files with count &gt; 0). */
    private static Map<String, Integer> scanSources(Path repoRoot, Path srcRoot, EffectTypes types)
            throws IOException {
        Map<String, Integer> perFile = new TreeMap<>();
        if (!Files.isDirectory(srcRoot)) {
            return perFile;
        }
        try (Stream<Path> walk = Files.walk(srcRoot)) {
            for (Path path : (Iterable<Path>) walk.filter(Files::isRegularFile)
                    .filter(p -> fileNameOf(p).endsWith(".java")).sorted()::iterator) {
                String rel = repoRoot.relativize(path).toString().replace('\\', '/');
                boolean exemptFile = rel.contains("/service/effect/") || rel.contains("/service/validate/");
                int count = 0;
                Matcher m = INSTANCEOF_RE.matcher(readText(path));
                while (m.find()) {
                    String token = lastSegment(m.group(1));
                    if (!types.all().contains(token)) {
                        continue; // not an effect type at all
                    }
                    if (types.wrapperOrInterface().contains(token)) {
                        continue; // structural wrapper or capability interface
                    }
                    if (exemptFile) {
                        continue; // sanctioned registry zone
                    }
                    count++;
                }
                if (count > 0) {
                    perFile.put(rel, count);
                }
            }
        }
        return perFile;
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

    private static String lastSegment(String qualified) {
        int dot = qualified.lastIndexOf('.');
        return dot < 0 ? qualified : qualified.substring(dot + 1);
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
