package com.github.laxika.magicalvibes.architecture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Ratchet against hand-rolled game-loss decisions.
 *
 * <p>Losing the game is a chain, not a boolean: the blanket "can't lose" effects (Platinum Angel),
 * then the prevention that only some reasons allow (Phyrexian Unlife stops the life loss but not
 * poison), then every registered {@code LossReplacer} (Lich's Mirror's reset). Each of those had
 * to be remembered, in order, at nine separate call sites — and one of them silently wasn't:
 * {@code StateBasedActionService.checkEmptyLibraryLoss} consulted the can't-lose effects but never
 * the replacers, and only failed to be a live bug because the reset happened to clear the flag it
 * keyed off.
 *
 * <p>So {@code GameQueryService.canPlayerLoseGame} / {@code canPlayerLoseFromLife} are now private
 * to the one gate that runs the whole chain. Everything else asks
 * {@code GameOutcomeService.resolveLoss(gameData, playerId, reason)} and acts on the
 * {@code LossOutcome}. Win-side checks ("your opponents can't win the game") ask
 * {@code GameOutcomeService.canPlayerWinGame} instead — a win effect ends the game immediately
 * rather than making anyone lose, so it deliberately skips the replacer chain.
 */
class LossDispatchRatchetTest {

    private static final String ENGINE_SERVICE_PKG =
            "magical-vibes-engine/src/main/java/com/github/laxika/magicalvibes/service";

    /**
     * {@code GameQueryService} declares the two queries; {@code GameOutcomeService} is the single
     * gate allowed to consume them.
     */
    private static final Set<String> SANCTIONED_FILES =
            Set.of("GameQueryService.java", "GameOutcomeService.java");

    private static final Pattern LOSS_QUERY_RE =
            Pattern.compile("\\bcanPlayerLose(Game|FromLife)\\s*\\(");

    @Test
    @DisplayName("Only GameOutcomeService decides whether a player loses the game")
    void lossQueriesAreNotCalledOutsideTheGate() throws IOException {
        Path servicePkg = locateRepoRoot().resolve(ENGINE_SERVICE_PKG);
        assertThat(servicePkg).isDirectory();

        List<String> offenders = new ArrayList<>();
        try (Stream<Path> files = Files.walk(servicePkg)) {
            for (Path path : (Iterable<Path>) files
                    .filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".java"))
                    .filter(file -> !SANCTIONED_FILES.contains(file.getFileName().toString()))
                    .sorted()::iterator) {
                String source = Files.readString(path, StandardCharsets.UTF_8);
                if (LOSS_QUERY_RE.matcher(source).find()) {
                    offenders.add(servicePkg.relativize(path).toString().replace('\\', '/'));
                }
            }
        }

        assertThat(offenders)
                .withFailMessage(() -> "Loss-dispatch ratchet failed — these files decide a game loss "
                        + "themselves instead of going through the shared gate:\n  "
                        + String.join("\n  ", offenders)
                        + "\n\nCall GameOutcomeService.resolveLoss(gameData, playerId, LossReason.X) and "
                        + "branch on the LossOutcome: only LOSES means finish the game. Calling "
                        + "canPlayerLoseGame directly skips every registered LossReplacer (Lich's Mirror), "
                        + "which is exactly the bug this ratchet exists to prevent. For a win-side check "
                        + "(\"your opponents can't win the game\"), use GameOutcomeService.canPlayerWinGame.")
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
