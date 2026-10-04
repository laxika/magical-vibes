package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.ScavengerGrounds;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GauFeralYouth.class, ScavengerGrounds.class})
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

    @Test
    void triggersDuringOpponentsEndStepForItsControllersGraveyard() {
        addReadyGau();
        gd.playersWhoseCardsLeftGraveyardThisTurn.add(player1.getId());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    void opponentsGraveyardDoesNotSatisfyCondition() {
        addReadyGau();
        gd.playersWhoseCardsLeftGraveyardThisTurn.add(player2.getId());

        advanceToEndStep();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    void damageUsesPowerWhenAbilityResolves() {
        Permanent gau = addReadyGau();
        gd.playersWhoseCardsLeftGraveyardThisTurn.add(player1.getId());
        advanceToEndStep();
        assertThat(gd.stack).hasSize(1);
        gau.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.passBothPriorities();

        harness.assertLife(player2, 15);
    }

    @Test
    void damageUsesLastKnownPowerAfterGauLeavesBattlefield() {
        Permanent gau = addReadyGau();
        gd.playersWhoseCardsLeftGraveyardThisTurn.add(player1.getId());
        advanceToEndStep();
        assertThat(gd.stack).hasSize(1);
        gau.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, gau);

        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        harness.assertInGraveyard(player1, "Gau, Feral Youth");
    }

    @Test
    void cardLeavingGraveyardAfterEndStepBeginsDoesNotTrigger() {
        addReadyGau();
        harness.addToBattlefield(player1, new ScavengerGrounds());
        harness.setGraveyard(player1, List.of(new GauFeralYouth()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        advanceToEndStep();
        assertThat(gd.stack).isEmpty();

        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    void exilingMultipleCardsFromGraveyardProducesOnlyOneEndStepTrigger() {
        addReadyGau();
        harness.addToBattlefield(player1, new ScavengerGrounds());
        harness.setGraveyard(player1, List.of(new GauFeralYouth(), new GauFeralYouth()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        advanceToEndStep();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    private Permanent addReadyGau() {
        return addCreatureReady(player1, new GauFeralYouth());
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
    }
}
