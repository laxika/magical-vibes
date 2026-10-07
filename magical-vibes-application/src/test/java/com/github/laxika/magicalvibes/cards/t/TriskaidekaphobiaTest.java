package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Triskaidekaphobia.class})
class TriskaidekaphobiaTest extends BaseCardTest {

    private static final String GAIN_MODE =
            "Each player with exactly 13 life loses the game, then each player gains 1 life.";
    private static final String LOSE_MODE =
            "Each player with exactly 13 life loses the game, then each player loses 1 life.";

    @Test
    @DisplayName("Gain mode: nobody at 13 — each player gains 1 life")
    void gainModeNobodyAtThirteen() {
        harness.addToBattlefield(player1, new Triskaidekaphobia());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

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
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

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
        harness.setLife(player1, 1);
        harness.setLife(player2, 13);

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
        harness.setLife(player1, 13);
        harness.setLife(player2, 13);

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

    @Test
    @DisplayName("Mode is chosen before players can respond to the upkeep trigger")
    void modeIsChosenWhenTriggerIsPutOnStack() {
        harness.addToBattlefield(player1, new Triskaidekaphobia());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleListChoice(player1, GAIN_MODE);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 21);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Controller at 13 loses in gain mode")
    void controllerAtThirteenLosesBeforeLifeGain() {
        harness.addToBattlefield(player1, new Triskaidekaphobia());
        harness.setLife(player1, 13);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, GAIN_MODE);

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
        harness.assertLife(player1, 13);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Reaching 13 from life gain does not immediately lose the game")
    void reachingThirteenFromLifeGainDoesNotLose() {
        harness.addToBattlefield(player1, new Triskaidekaphobia());
        harness.setLife(player1, 12);
        harness.setLife(player2, 14);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, GAIN_MODE);

        harness.assertLife(player1, 13);
        harness.assertLife(player2, 15);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Reaching 13 from life loss does not immediately lose the game")
    void reachingThirteenFromLifeLossDoesNotLose() {
        harness.addToBattlefield(player1, new Triskaidekaphobia());
        harness.setLife(player1, 14);
        harness.setLife(player2, 12);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, LOSE_MODE);

        harness.assertLife(player1, 13);
        harness.assertLife(player2, 11);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Both players reaching zero from lose mode draw the game")
    void bothAtOneDrawAfterLifeLoss() {
        harness.addToBattlefield(player1, new Triskaidekaphobia());
        harness.setLife(player1, 1);
        harness.setLife(player2, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, LOSE_MODE);

        harness.assertLife(player1, 0);
        harness.assertLife(player2, 0);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isNull();
    }
}
