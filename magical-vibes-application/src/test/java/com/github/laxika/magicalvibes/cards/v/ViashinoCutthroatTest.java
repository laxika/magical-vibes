package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ViashinoCutthroat.class)
class ViashinoCutthroatTest extends BaseCardTest {

    @Test
    @DisplayName("Can attack on the turn it enters because of haste")
    void canAttackImmediately() {
        Permanent cutthroat = harness.addToBattlefieldAndReturn(player1, new ViashinoCutthroat());
        cutthroat.setSummoningSick(true);

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(cutthroat.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Returns to its owner's hand even if control changes after triggering")
    void returnsToOwnerAfterControlChanges() {
        ViashinoCutthroat card = new ViashinoCutthroat();
        card.setOwnerId(player1.getId());
        Permanent cutthroat = harness.addToBattlefieldAndReturn(player1, card);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(cutthroat);
        gd.playerBattlefields.get(player2.getId()).add(cutthroat);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Viashino Cutthroat");
        harness.assertInHand(player1, "Viashino Cutthroat");
        harness.assertNotInHand(player2, "Viashino Cutthroat");
    }

    @Test
    @DisplayName("Entering after the end step begins waits until the next end step")
    void enteringDuringEndStepWaitsForNextEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.enterBattlefieldAndReturn(player1, new ViashinoCutthroat());

        assertThat(gd.stack).isEmpty();
        harness.passUntil(player1, TurnStep.CLEANUP);
        harness.assertOnBattlefield(player1, "Viashino Cutthroat");
        harness.assertNotInHand(player1, "Viashino Cutthroat");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Viashino Cutthroat");
        harness.assertInHand(player1, "Viashino Cutthroat");
    }

    @Test
    @DisplayName("Triggers at end step and returns itself to owner's hand")
    void triggersAtEndStepAndReturnsToHand() {
        Permanent cutthroat = harness.addToBattlefieldAndReturn(player1, new ViashinoCutthroat());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getSourcePermanentId()).isEqualTo(cutthroat.getId());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Viashino Cutthroat");
        harness.assertInHand(player1, "Viashino Cutthroat");
    }

    @Test
    @DisplayName("Does not return a new object after the original leaves before the trigger resolves")
    void doesNotReturnNewObjectAfterOriginalLeavesBeforeTriggerResolves() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new ViashinoCutthroat());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(original);
        Permanent replacement = harness.addToBattlefieldAndReturn(player1, new ViashinoCutthroat());

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(replacement);
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(card -> card == original.getCard());
    }

    @Test
    @DisplayName("Triggers on the opponent's end step")
    void triggersOnOpponentsEndStep() {
        harness.addToBattlefield(player1, new ViashinoCutthroat());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Viashino Cutthroat");
        harness.assertInHand(player1, "Viashino Cutthroat");
    }
}
