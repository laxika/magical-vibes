package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RazorPendulumTest extends BaseCardTest {

    private void advanceToEndStepTrigger(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to END_STEP, trigger fires onto stack
        harness.passBothPriorities(); // resolve trigger
    }

    @Test
    @DisplayName("Deals 2 damage to the end-step player at 5 or less life")
    void damagesLowLifeEndStepPlayer() {
        harness.addToBattlefield(player1, new RazorPendulum());
        harness.setLife(player1, 5);
        harness.setLife(player2, 20);

        advanceToEndStepTrigger(player1);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does nothing when the end-step player has more than 5 life")
    void noDamageAboveThreshold() {
        harness.addToBattlefield(player1, new RazorPendulum());
        harness.setLife(player1, 6);

        advanceToEndStepTrigger(player1);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(6);
    }

    @Test
    @DisplayName("Hits the end-step player even when an opponent controls it")
    void hitsEndStepPlayerNotController() {
        harness.addToBattlefield(player2, new RazorPendulum());
        harness.setLife(player1, 4);
        harness.setLife(player2, 4);

        advanceToEndStepTrigger(player1);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(4);
    }
}
