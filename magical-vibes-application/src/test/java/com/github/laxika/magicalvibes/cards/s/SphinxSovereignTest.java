package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SphinxSovereignTest extends BaseCardTest {

    private void resolveEndStepTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to END_STEP, trigger fires onto stack
        harness.passBothPriorities(); // resolve trigger
    }

    @Test
    @DisplayName("Untapped at end step: controller gains 3 life, opponent unaffected")
    void untappedGainsThreeLife() {
        harness.addToBattlefield(player1, new SphinxSovereign());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveEndStepTrigger();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Tapped at end step: each opponent loses 3 life, controller unaffected")
    void tappedDrainsOpponents() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new SphinxSovereign());
        sphinx.tap();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveEndStepTrigger();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }
}
