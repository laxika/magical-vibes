package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CaptivatingVampire;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StromkirkBloodthief.class, CaptivatingVampire.class, GrizzlyBears.class})
class StromkirkBloodthiefTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on a Vampire you control when an opponent lost life")
    void putsCounterOnTargetVampire() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CaptivatingVampire());
        Permanent bloodthief = addBloodthief();
        gd.lifeLostThisTurn.put(player2.getId(), 1);

        advanceToEndStep();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bloodthief.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger when no opponent lost life")
    void doesNotTriggerWithoutOpponentLifeLoss() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CaptivatingVampire());
        addBloodthief();

        advanceToEndStep();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Only offers Vampires the controller controls as targets")
    void targetIsControlledVampire() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CaptivatingVampire());
        Permanent nonVampire = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentVampire = harness.addToBattlefieldAndReturn(player2, new CaptivatingVampire());
        Permanent bloodthief = addBloodthief();
        gd.lifeLostThisTurn.put(player2.getId(), 1);

        advanceToEndStep();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(bloodthief.getId(), target.getId());
        assertThat(choice.validIds()).doesNotContain(nonVampire.getId(), opponentVampire.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can put the counter on itself and grants only one counter for multiple life lost")
    void targetsItselfAfterMultipleLifeLost() {
        Permanent bloodthief = addBloodthief();
        harness.inMutationScope(() ->
                harness.getLifeSupport().applyLifeLoss(gd, player2.getId(), 5, "test"));

        advanceToEndStep();
        harness.handlePermanentChosen(player1, bloodthief.getId());
        harness.passBothPriorities();

        assertThat(bloodthief.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger for its controller's life loss")
    void doesNotTriggerForControllerLifeLoss() {
        Permanent bloodthief = addBloodthief();
        harness.inMutationScope(() ->
                harness.getLifeSupport().applyLifeLoss(gd, player1.getId(), 1, "test"));

        advanceToEndStep();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(bloodthief.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger during the opponent's end step")
    void doesNotTriggerOnOpponentsEndStep() {
        Permanent bloodthief = addBloodthief();
        harness.inMutationScope(() ->
                harness.getLifeSupport().applyLifeLoss(gd, player2.getId(), 1, "test"));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(bloodthief.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Life gained back before resolution does not undo earlier life loss")
    void stillResolvesAfterOpponentGainsLifeBack() {
        Permanent bloodthief = addBloodthief();
        harness.inMutationScope(() ->
                harness.getLifeSupport().applyLifeLoss(gd, player2.getId(), 1, "test"));

        advanceToEndStep();
        harness.handlePermanentChosen(player1, bloodthief.getId());
        harness.inMutationScope(() ->
                harness.getLifeSupport().applyGainLife(gd, player2.getId(), 3));
        harness.passBothPriorities();

        assertThat(bloodthief.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A trigger still resolves on another Vampire after its source leaves")
    void triggerResolvesAfterSourceLeaves() {
        Permanent bloodthief = addBloodthief();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new StromkirkBloodthief());
        harness.inMutationScope(() ->
                harness.getLifeSupport().applyLifeLoss(gd, player2.getId(), 1, "test"));

        advanceToEndStep();
        harness.handlePermanentChosen(player1, target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bloodthief));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The ability does not redirect its counter when its target leaves")
    void doesNotRedirectCounterWhenTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CaptivatingVampire());
        Permanent bloodthief = addBloodthief();
        harness.inMutationScope(() ->
                harness.getLifeSupport().applyLifeLoss(gd, player2.getId(), 1, "test"));

        advanceToEndStep();
        harness.handlePermanentChosen(player1, target.getId());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, target));
        harness.passBothPriorities();

        assertThat(bloodthief.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addBloodthief() {
        return harness.addToBattlefieldAndReturn(player1, new StromkirkBloodthief());
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
    }
}
