package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GargoyleFlock.class, GrizzlyBears.class})
class GargoyleFlockTest extends BaseCardTest {

    @Test
    void createsFlyingBlueTyranidGargoyleWhenCreatureEnteredThisTurn() {
        harness.addToBattlefield(player1, new GargoyleFlock());
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        resolveControllerEndStep();

        Permanent token = findPermanent(player1, "Tyranid Gargoyle");
        assertThat(token.getCard().getColors()).containsExactly(CardColor.BLUE);
        assertThat(token.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.TYRANID, CardSubtype.GARGOYLE);
        assertThat(token.getEffectivePower()).isEqualTo(1);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
    }

    @Test
    void doesNotCreateTokenWithoutCreatureEnteringThisTurn() {
        harness.addToBattlefield(player1, new GargoyleFlock());

        resolveControllerEndStep();

        assertThat(countPermanents(player1, "Tyranid Gargoyle")).isZero();
    }

    @Test
    void doesNotCountOpponentCreatureEnteringThisTurn() {
        harness.addToBattlefield(player1, new GargoyleFlock());
        harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        resolveControllerEndStep();

        assertThat(countPermanents(player1, "Tyranid Gargoyle")).isZero();
    }

    @Test
    void countsItsOwnEntryThisTurn() {
        harness.enterBattlefieldAndReturn(player1, new GargoyleFlock());

        resolveControllerEndStep();

        assertThat(countPermanents(player1, "Tyranid Gargoyle")).isEqualTo(1);
    }

    @Test
    void createsOnlyOneTokenEvenWhenMultipleCreaturesEntered() {
        harness.addToBattlefield(player1, new GargoyleFlock());
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        resolveControllerEndStep();

        assertThat(countPermanents(player1, "Tyranid Gargoyle")).isEqualTo(1);
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        harness.enterBattlefieldAndReturn(player1, new GargoyleFlock());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Tyranid Gargoyle")).isZero();
        assertThat(countPermanents(player2, "Tyranid Gargoyle")).isZero();
    }

    @Test
    void creatureEnteringAfterEndStepBeginsDoesNotCauseTrigger() {
        harness.addToBattlefield(player1, new GargoyleFlock());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Tyranid Gargoyle")).isZero();
    }

    @Test
    void countsCreatureThatEnteredEvenIfItDiedBeforeEndStep() {
        harness.addToBattlefield(player1, new GargoyleFlock());
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setMarkedDamage(2);
        harness.runStateBasedActions();
        assertThat(countPermanents(player1, "Grizzly Bears")).isZero();

        resolveControllerEndStep();

        assertThat(countPermanents(player1, "Tyranid Gargoyle")).isEqualTo(1);
    }

    private void resolveControllerEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
