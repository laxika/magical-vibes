package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FirstResponder.class, GrizzlyBears.class})
class FirstResponderTest extends BaseCardTest {

    @Test
    void returnsAnotherCreatureAndGetsCountersEqualToItsPower() {
        Permanent responder = harness.addToBattlefieldAndReturn(player1, new FirstResponder());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToEndStep(player1);

        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(bears.getId());

        harness.handlePermanentChosen(player1, bears.getId());

        assertThat(gd.playerHands.get(player1.getId())).contains(bears.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
        assertThat(responder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void decliningLeavesBothCreaturesUnchanged() {
        Permanent responder = harness.addToBattlefieldAndReturn(player1, new FirstResponder());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToEndStep(player1);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(responder, bears);
        assertThat(responder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void withNoOtherCreatureAcceptedAbilityDoesNothing() {
        Permanent responder = harness.addToBattlefieldAndReturn(player1, new FirstResponder());

        advanceToEndStep(player1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(responder);
        assertThat(responder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void usesPowerOnBattlefieldIncludingCountersAndTemporaryModifiers() {
        Permanent responder = harness.addToBattlefieldAndReturn(player1, new FirstResponder());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.getCounters().put(CounterType.PLUS_ONE_PLUS_ONE, 2);
        bears.setPowerModifier(3);

        advanceToEndStep(player1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());

        assertThat(gd.playerHands.get(player1.getId())).contains(bears.getCard());
        assertThat(responder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
    }

    @Test
    void negativePowerReturnsCreatureWithoutAddingCounters() {
        Permanent responder = harness.addToBattlefieldAndReturn(player1, new FirstResponder());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setPowerModifier(-3);

        advanceToEndStep(player1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());

        assertThat(gd.playerHands.get(player1.getId())).contains(bears.getCard());
        assertThat(responder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void cannotChooseOpponentsCreature() {
        Permanent responder = harness.addToBattlefieldAndReturn(player1, new FirstResponder());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToEndStep(player1);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(ownBears.getId());
        harness.handlePermanentChosen(player1, ownBears.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingBears);
        assertThat(responder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        Permanent responder = harness.addToBattlefieldAndReturn(player1, new FirstResponder());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToEndStep(player2);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(responder, bears);
        assertThat(responder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void sourceLeavingStillAllowsReturnButDoesNotPutCountersOnNewPermanent() {
        Permanent responder = harness.addToBattlefieldAndReturn(player1, new FirstResponder());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToEndStep(player1);
        harness.getPermanentRemovalService().removePermanentToHand(gd, responder);
        gd.playerHands.get(player1.getId()).remove(responder.getCard());
        Permanent newResponder = harness.addToBattlefieldAndReturn(player1, responder.getCard());
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(bears.getId(), newResponder.getId());
        harness.handlePermanentChosen(player1, bears.getId());

        assertThat(gd.playerHands.get(player1.getId())).contains(bears.getCard());
        assertThat(newResponder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void advanceToEndStep(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
