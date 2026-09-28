package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(GauFeralYouth.class)
class GauFeralYouthTest extends BaseCardTest {

    @Test
    void attackingPutsPlusOnePlusOneCounterOnGau() {
        Permanent gau = addReadyGau();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gau.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void dealsPowerDamageAtEndStepAfterCardLeavesGraveyard() {
        Permanent gau = addReadyGau();
        gau.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playersWhoseCardsLeftGraveyardThisTurn.add(player1.getId());

        advanceToEndStep();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
    }

    @Test
    void doesNotDealEndStepDamageWithoutCardLeavingGraveyard() {
        addReadyGau();

        advanceToEndStep();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    private Permanent addReadyGau() {
        return addCreatureReady(player1, new GauFeralYouth());
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
