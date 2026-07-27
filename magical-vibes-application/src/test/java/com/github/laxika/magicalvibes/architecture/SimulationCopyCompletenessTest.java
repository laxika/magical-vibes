package com.github.laxika.magicalvibes.architecture;

import com.github.laxika.magicalvibes.model.GameData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Ratchet for {@link GameData#simulationCopy()}, which copies ~250 fields by hand. Nothing about
 * a hand-maintained parallel field list is self-correcting: a field added to {@code GameData}
 * that nobody remembers to copy simply vanishes from every MCTS simulation copy, silently, and
 * the AI plans against a board that differs from the real one.
 *
 * <p>That had already happened — 24 live fields were uncopied when this test was written,
 * including {@code lifeLostThisTurn}, {@code skipNextCombatPhaseCount} and the four
 * {@code spellCast*} payment-tracking maps, all of which persist across a turn and are read by
 * {@code ConditionEvaluationService} / {@code AmountEvaluationService} / {@code TurnProgressionService}.
 *
 * <p>{@code Card} is protected from the same failure mode by {@code CardFreezeTest}; this is the
 * equivalent for {@code GameData}. Unlike a bare field-count tripwire it names the offending field.
 *
 * <p>To add a field that genuinely must not be copied, add it to {@link #DELIBERATELY_NOT_COPIED}
 * with the reason — that list is the documentation of what simulation deliberately drops.
 */
class SimulationCopyCompletenessTest {

    /**
     * Fields {@code simulationCopy()} intentionally does not carry. Each entry needs a reason;
     * an entry without one is a bug being hidden rather than a decision being recorded.
     */
    private static final Map<String, String> DELIBERATELY_NOT_COPIED = Map.of(
            "createdAt",
            "Creation metadata; final and assigned by the constructor the copy already calls.",
            "layeredBoardCache",
            "A simulation copy must start with a cold CR 613 board cache so a simulated board can "
                    + "never be served for the real game or vice versa (documented on the field).");

    private static final String GAME_DATA_SRC =
            "magical-vibes-domain/src/main/java/com/github/laxika/magicalvibes/model/GameData.java";

    @Test
    @DisplayName("simulationCopy() references every GameData instance field")
    void simulationCopyCoversEveryField() throws IOException {
        String body = stripComments(extractSimulationCopyBody(readGameDataSource()));

        List<String> missing = new ArrayList<>();
        for (Field field : GameData.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            String name = field.getName();
            if (DELIBERATELY_NOT_COPIED.containsKey(name)) {
                continue;
            }
            if (!Pattern.compile("\\b" + Pattern.quote(name) + "\\b").matcher(body).find()) {
                missing.add(name);
            }
        }

        assertThat(missing)
                .as("These GameData fields are never referenced in simulationCopy(), so AI "
                        + "simulation copies silently lose them. Copy each one, or add it to "
                        + "DELIBERATELY_NOT_COPIED with a reason.")
                .isEmpty();
    }

    @Test
    @DisplayName("Every deliberately-uncopied field still exists on GameData")
    void allowlistHasNoStaleEntries() {
        Set<String> declared = new java.util.HashSet<>();
        for (Field field : GameData.class.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())) {
                declared.add(field.getName());
            }
        }
        assertThat(declared)
                .as("DELIBERATELY_NOT_COPIED names a field that no longer exists — drop the entry")
                .containsAll(DELIBERATELY_NOT_COPIED.keySet());
    }

    private static String readGameDataSource() throws IOException {
        Path source = locateRepoRoot().resolve(GAME_DATA_SRC);
        assertThat(Files.exists(source)).as("GameData source not found at %s", source).isTrue();
        return Files.readString(source, StandardCharsets.UTF_8);
    }

    /** Extracts the text of {@code simulationCopy()} by brace-matching from its declaration. */
    private static String extractSimulationCopyBody(String source) {
        int start = source.indexOf("public GameData simulationCopy()");
        assertThat(start).as("simulationCopy() declaration not found in GameData").isNotEqualTo(-1);

        int open = source.indexOf('{', start);
        int depth = 0;
        for (int i = open; i < source.length(); i++) {
            char c = source.charAt(i);
            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return source.substring(open, i + 1);
                }
            }
        }
        throw new IllegalStateException("Unbalanced braces in simulationCopy()");
    }

    /** Drops block and line comments so a field merely NAMED in a comment does not count. */
    private static String stripComments(String code) {
        return code.replaceAll("(?s)/\\*.*?\\*/", " ").replaceAll("//[^\n]*", " ");
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
