package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CaptivatingVampire;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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

    private Permanent addBloodthief() {
        return harness.addToBattlefieldAndReturn(player1, new StromkirkBloodthief());
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
