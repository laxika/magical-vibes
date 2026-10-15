package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(StitchInTime.class)
class StitchInTimeTest extends BaseCardTest {

    @Test
    @DisplayName("A won flip grants an extra turn, while a lost flip does not")
    void coinFlipControlsExtraTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new StitchInTime(), "{1}{U}{R}");
        harness.passBothPriorities();

        boolean won = gameLogContains("wins the coin flip");
        if (won) {
            assertThat(gd.extraTurns).containsExactly(player1.getId());
        } else {
            assertThat(gameLogContains("loses the coin flip")).isTrue();
            assertThat(gd.extraTurns).isEmpty();
        }
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The flip happens on resolution and preserves previously scheduled extra turns")
    void resolvingForSecondPlayerPreservesQueuedTurns() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gd.queueExtraTurnFirst(player1.getId(), false);

        harness.castFromHand(player2, new StitchInTime(), "{1}{U}{R}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gameLogContains("wins the coin flip")).isFalse();
        assertThat(gameLogContains("loses the coin flip")).isFalse();
        assertThat(gd.extraTurns).containsExactly(player1.getId());

        harness.passBothPriorities();

        if (gameLogContains("wins the coin flip")) {
            assertThat(gd.extraTurns).containsExactly(player2.getId(), player1.getId());
        } else {
            assertThat(gameLogContains("loses the coin flip")).isTrue();
            assertThat(gd.extraTurns).containsExactly(player1.getId());
        }
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Stitch in Time");
        harness.assertNotInGraveyard(player1, "Stitch in Time");
    }
}
