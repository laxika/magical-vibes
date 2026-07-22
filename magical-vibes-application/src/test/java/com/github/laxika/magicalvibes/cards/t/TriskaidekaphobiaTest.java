package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TriskaidekaphobiaTest extends BaseCardTest {

    private static final String GAIN_MODE =
            "Each player with exactly 13 life loses the game, then each player gains 1 life.";
    private static final String LOSE_MODE =
            "Each player with exactly 13 life loses the game, then each player loses 1 life.";

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP, trigger fires
    }

    @Test
    @DisplayName("Gain mode: nobody at 13 — each player gains 1 life")
    void gainModeNobodyAtThirteen() {
        harness.addToBattlefield(player1, new Triskaidekaphobia());
        gd.playerLifeTotals.put(player1.getId(), 20);
        gd.playerLifeTotals.put(player2.getId(), 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, GAIN_MODE);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(21);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Lose mode: nobody at 13 — each player loses 1 life")
    void loseModeNobodyAtThirteen() {
        harness.addToBattlefield(player1, new Triskaidekaphobia());
        gd.playerLifeTotals.put(player1.getId(), 20);
        gd.playerLifeTotals.put(player2.getId(), 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, LOSE_MODE);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Opponent at 13 loses before life adjustment (lose mode)")
    void opponentAtThirteenLosesBeforeLifeLoss() {
        harness.addToBattlefield(player1, new Triskaidekaphobia());
        gd.playerLifeTotals.put(player1.getId(), 1);
        gd.playerLifeTotals.put(player2.getId(), 13);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, LOSE_MODE);

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        // Controller survives at 1 — life adjust never applied after the loss.
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Both at 13 — game is a draw")
    void bothAtThirteenIsDraw() {
        harness.addToBattlefield(player1, new Triskaidekaphobia());
        gd.playerLifeTotals.put(player1.getId(), 13);
        gd.playerLifeTotals.put(player2.getId(), 13);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, GAIN_MODE);

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new Triskaidekaphobia());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
