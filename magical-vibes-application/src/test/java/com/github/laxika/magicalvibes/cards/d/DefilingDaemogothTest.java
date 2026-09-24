package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DefilingDaemogoth.class, GrizzlyBears.class})
class DefilingDaemogothTest extends BaseCardTest {

    @Test
    @DisplayName("Gains one life for each creature that deals combat damage, then drains opponents by that amount")
    void gainsLifeAndDrainsOpponentsAtEndStep() {
        harness.addToBattlefield(player1, new DefilingDaemogoth());
        addReadyAttacker(new GrizzlyBears());
        addReadyAttacker(new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);

        resolveEndStepTrigger();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    private void addReadyAttacker(com.github.laxika.magicalvibes.model.Card card) {
        var attacker = addCreatureReady(player1, card);
        attacker.setAttacking(true);
    }

    private void resolveEndStepTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
