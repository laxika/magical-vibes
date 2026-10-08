package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnstoppablePlan.class, GrizzlyBears.class, AngelsFeather.class, Island.class})
class UnstoppablePlanTest extends BaseCardTest {

    private void advanceToEndStepTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Untaps all nonland permanents you control at the beginning of your end step")
    void untapsControlledNonlandPermanents() {
        harness.addToBattlefield(player1, new UnstoppablePlan());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AngelsFeather());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        creature.tap();
        artifact.tap();
        land.tap();

        advanceToEndStepTrigger();

        assertThat(creature.isTapped()).isFalse();
        assertThat(artifact.isTapped()).isFalse();
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not trigger during an opponent's end step")
    void doesNotTriggerDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new UnstoppablePlan());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.tap();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untaps itself but leaves opposing nonland permanents tapped")
    void untapsItselfWithoutUntappingOpponentsPermanents() {
        Permanent plan = harness.addToBattlefieldAndReturn(player1, new UnstoppablePlan());
        Permanent opposingPlan = harness.addToBattlefieldAndReturn(player2, new UnstoppablePlan());
        plan.tap();
        opposingPlan.tap();

        advanceToEndStepTrigger();

        assertThat(plan.isTapped()).isFalse();
        assertThat(opposingPlan.isTapped()).isTrue();
    }

    @Test
    @DisplayName("End step ability uses the current battlefield even after its source leaves")
    void resolvesAfterSourceLeavesAndUntapsNewPermanents() {
        Permanent plan = harness.addToBattlefieldAndReturn(player1, new UnstoppablePlan());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(plan);
        gd.playerGraveyards.get(player1.getId()).add(plan.getCard());
        Permanent newPlan = harness.addToBattlefieldAndReturn(player1, new UnstoppablePlan());
        newPlan.tap();

        harness.passBothPriorities();

        assertThat(newPlan.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Untapping waits for the end step ability to resolve")
    void waitsForTriggerResolution() {
        Permanent plan = harness.addToBattlefieldAndReturn(player1, new UnstoppablePlan());
        plan.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(1);
        assertThat(plan.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(plan.isTapped()).isFalse();
    }
}
