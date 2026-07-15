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
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PERMANENT guard for the completed TargetSpec migration (refactor step 11 close-out).
 *
 * <p>The eleven legacy per-effect targeting methods on {@code CardEffect}
 * ({@code canTargetPlayer}, {@code canTargetPermanent}, {@code canTargetSpell},
 * {@code canTargetGraveyard}, {@code canTargetAnyGraveyard}, {@code targetsControllersGraveyardOnly},
 * {@code canTargetExile}, {@code targetPredicate}, {@code isSelfTargeting},
 * {@code requiredPlayerTargetCount}, {@code isDamageOrDestruction}) were DELETED in step 10; every
 * effect now exposes its targeting through the single declarative {@code TargetSpec targetSpec()}.
 * The migration is done, so this is no longer a shrinking ratchet against a baseline file — it is the
 * permanent invariant guard that keeps the new mechanism from eroding. It enforces two cheap,
 * statically-checkable invariants:
 *
 * <ol>
 *   <li><b>No reintroduction of a legacy targeting method.</b> No file under
 *       {@code model/effect/} may declare a method named {@code canTargetPlayer} /
 *       {@code canTargetPermanent} / … (the eleven above). This prevents a future change from
 *       quietly bringing back the per-effect targeting booleans the program deleted. (Record
 *       COMPONENTS named {@code canTargetSpell} / {@code targetPredicate} are NOT method
 *       declarations with a body and do not match — the two documented duals
 *       {@code ChangeColorTextEffect} / {@code PutCounterOnTargetPermanentEffect} keep theirs.)</li>
 *   <li><b>Every {@code @ValidatesTarget} effect declares a non-NONE {@code targetSpec()}.</b> A
 *       hand-written {@code @ValidatesTarget} validator is now ONLY an escape hatch for
 *       non-structural rules (opponent-relation, controller/owner compare, chosen-source,
 *       null-target tolerance) — and such an effect must STILL declare its structural spec so the
 *       declarative interpreter offers/type-checks it. This invariant forbids a validator-only
 *       effect that bypasses {@code targetSpec()} entirely. The two documented equip/attach
 *       validators ({@code StaticBoostEffect}, {@code AttachedBoostEffect}) are exempt: they target
 *       through the equip/attach mechanism, not the single-target pipeline, so they carry no
 *       {@code targetSpec()} category by design.</li>
 * </ol>
 *
 * <p><b>LOCKSTEP:</b> both invariants are duplicated in {@code scripts/targetspec-audit.py}
 * (same legacy-method set, same {@code public <boolean|int|PermanentPredicate> name()} override
 * regex, same {@code targetSpec()} brace-matched non-NONE detection, same two exempt effects). If
 * you change either invariant here you MUST change it there in lockstep, or the script and this test
 * will disagree.
 */
class TargetSpecRatchetTest {

    /** The eleven legacy targeting methods the migration deleted (isPowerToughnessDefining is KEPT). */
    private static final List<String> LEGACY_METHODS = List.of(
            "canTargetPlayer", "canTargetPermanent", "canTargetSpell",
            "canTargetGraveyard", "canTargetAnyGraveyard", "targetsControllersGraveyardOnly",
            "canTargetExile", "targetPredicate", "isSelfTargeting",
            "requiredPlayerTargetCount", "isDamageOrDestruction");

    /**
     * Effects that carry a {@code @ValidatesTarget} validator but legitimately expose no
     * {@code targetSpec()} category: they are targeted through the equip/attach mechanism, not the
     * single-target pipeline the spec interpreter drives. Exempt from invariant 2.
     */
    private static final Set<String> EQUIP_ATTACH_VALIDATED_EFFECTS =
            Set.of("StaticBoostEffect", "AttachedBoostEffect");

    private static final String EFFECT_PKG =
            "magical-vibes-domain/src/main/java/com/github/laxika/magicalvibes/model/effect";
    private static final String VALIDATE_DIR =
            "magical-vibes-engine/src/main/java/com/github/laxika/magicalvibes/service/validate";

    private static final Pattern VALIDATES_TARGET_RE =
            Pattern.compile("@ValidatesTarget\\(\\s*([A-Za-z_]\\w*)\\.class");
    private static final Pattern TARGET_SPEC_METHOD_RE =
            Pattern.compile("TargetSpec\\s+targetSpec\\s*\\(\\s*\\)\\s*\\{");
    private static final Pattern SPEC_FACTORY_RE =
            Pattern.compile("\\b(?:benign|harmful)\\s*\\(");
    private static final Pattern SPEC_CATEGORY_RE =
            Pattern.compile("\\bTargetCategory\\.(\\w+)");

    @Test
    @DisplayName("No effect file declares any of the eleven deleted legacy targeting methods")
    void noEffectDeclaresALegacyTargetingMethod() throws IOException {
        Path repoRoot = locateRepoRoot();
        Path effectPkg = repoRoot.resolve(EFFECT_PKG);

        List<String> messages = new ArrayList<>();
        try (Stream<Path> files = Files.list(effectPkg)) {
            for (Path path : (Iterable<Path>) files.sorted()::iterator) {
                if (!fileNameOf(path).endsWith(".java")) {
                    continue;
                }
                Set<String> overridden = overriddenLegacyMethods(readText(path));
                if (!overridden.isEmpty()) {
                    String rel = repoRoot.relativize(path).toString().replace('\\', '/');
                    messages.add(String.format(
                            "%s declares deleted legacy targeting method(s): %s. These were removed in "
                                    + "TargetSpec migration step 10 — do NOT reintroduce them. Declare targeting "
                                    + "purely through targetSpec() (override it to return "
                                    + "TargetSpec.harmful(...)/benign(...) for the right TargetCategory).",
                            rel, String.join(", ", overridden)));
                }
            }
        }

        assertThat(messages)
                .withFailMessage(() -> "TargetSpec guard (invariant 1 — no legacy method) failed:\n  "
                        + String.join("\n  ", messages))
                .isEmpty();
    }

    @Test
    @DisplayName("Every @ValidatesTarget effect (bar equip/attach) declares a non-NONE targetSpec()")
    void everyValidatedEffectDeclaresANonNoneTargetSpec() throws IOException {
        Path repoRoot = locateRepoRoot();
        Path effectPkg = repoRoot.resolve(EFFECT_PKG);

        // Which effect classes are named by a @ValidatesTarget annotation.
        Set<String> validatedEffects = new TreeSet<>();
        try (Stream<Path> files = Files.walk(repoRoot.resolve(VALIDATE_DIR))) {
            for (Path path : (Iterable<Path>) files.filter(Files::isRegularFile)
                    .filter(p -> fileNameOf(p).endsWith(".java")).sorted()::iterator) {
                Matcher m = VALIDATES_TARGET_RE.matcher(readText(path));
                while (m.find()) {
                    validatedEffects.add(m.group(1));
                }
            }
        }

        List<String> messages = new ArrayList<>();
        for (String effect : validatedEffects) {
            if (EQUIP_ATTACH_VALIDATED_EFFECTS.contains(effect)) {
                continue;
            }
            Path effectFile = effectPkg.resolve(effect + ".java");
            if (!Files.exists(effectFile)) {
                messages.add(String.format(
                        "@ValidatesTarget names %s but %s/%s.java does not exist.",
                        effect, EFFECT_PKG, effect));
                continue;
            }
            if (!declaresNonNoneTargetSpec(readText(effectFile))) {
                messages.add(String.format(
                        "%s has a @ValidatesTarget validator but no non-NONE targetSpec(). A validator is now "
                                + "only an escape hatch for non-structural rules; the effect must STILL declare its "
                                + "structural spec (override targetSpec() to a benign(...)/harmful(...) category) so "
                                + "the declarative interpreter offers and type-checks it. If it genuinely targets "
                                + "outside the single-target pipeline (equip/attach), add it to "
                                + "EQUIP_ATTACH_VALIDATED_EFFECTS here and in scripts/targetspec-audit.py.",
                        effect));
            }
        }

        assertThat(messages)
                .withFailMessage(() -> "TargetSpec guard (invariant 2 — validator implies non-NONE spec) failed:\n  "
                        + String.join("\n  ", messages))
                .isEmpty();
    }

    /** Distinct legacy targeting methods the file declares as an override (regex, body required). */
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

    /**
     * True iff the file overrides {@code targetSpec()} with a body that can return a non-NONE spec —
     * i.e. it uses a {@code benign(} / {@code harmful(} factory (both take a non-NONE category) or
     * names a {@code TargetCategory} value other than {@code NONE}. A conditional body with at least
     * one non-NONE branch (per-recipient / per-scope specs) counts as declaring a structural spec.
     * Mirrors {@code targetspec-audit.py}'s brace-matched detection exactly.
     */
    private static boolean declaresNonNoneTargetSpec(String text) {
        Matcher m = TARGET_SPEC_METHOD_RE.matcher(text);
        if (!m.find()) {
            return false;
        }
        String body = braceMatchedBody(text, m.end() - 1);
        if (SPEC_FACTORY_RE.matcher(body).find()) {
            return true;
        }
        Matcher cat = SPEC_CATEGORY_RE.matcher(body);
        while (cat.find()) {
            if (!"NONE".equals(cat.group(1))) {
                return true;
            }
        }
        return false;
    }

    /** Body between the matching braces starting at {@code openBraceIdx} (excludes the outer braces). */
    private static String braceMatchedBody(String text, int openBraceIdx) {
        int depth = 0;
        for (int i = openBraceIdx; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return text.substring(openBraceIdx + 1, i);
                }
            }
        }
        return text.substring(openBraceIdx + 1);
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
