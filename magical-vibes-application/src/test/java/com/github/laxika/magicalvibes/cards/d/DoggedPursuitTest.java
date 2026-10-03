package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DoggedPursuit.class})
class DoggedPursuitTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent loses 1 life and its controller gains 1 life at its end step")
    void drainsAtControllerEndStep() {
        harness.addToBattlefield(player1, new DoggedPursuit());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToEndStep(player1);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Does not trigger at an opponent's end step")
    void doesNotTriggerAtOpponentsEndStep() {
        harness.addToBattlefield(player1, new DoggedPursuit());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The other player's Dogged Pursuit drains at that player's end step")
    void drainsForOtherController() {
        harness.addToBattlefield(player2, new DoggedPursuit());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToEndStep(player2);

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 21);
    }

    @Test
    @DisplayName("Each copy triggers independently at its controller's end step")
    void multipleCopiesDrainIndependently() {
        harness.addToBattlefield(player1, new DoggedPursuit());
        harness.addToBattlefield(player1, new DoggedPursuit());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToEndStep(player1);

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("An end step trigger resolves after Dogged Pursuit leaves the battlefield")
    void triggerResolvesWithoutSource() {
        DoggedPursuit pursuit = new DoggedPursuit();
        harness.addToBattlefield(player1, pursuit);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToEndStep(player1);

        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).clear();
        gd.playerGraveyards.get(player1.getId()).add(pursuit);
        resolveAllTriggers();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
