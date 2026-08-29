package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
