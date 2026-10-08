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

@CardUsed(ViashinoSandscout.class)
class ViashinoSandscoutTest extends BaseCardTest {

    @Test
    @DisplayName("Can attack immediately due to haste")
    void canAttackImmediatelyDueToHaste() {
        harness.setLife(player2, 20);

        Permanent sandscout = harness.addToBattlefieldAndReturn(player1, new ViashinoSandscout());
        sandscout.setSummoningSick(true);
        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Triggers at end step and returns itself to owner's hand on resolution")
    void triggersAtEndStepAndReturnsItselfToHand() {
        Permanent sandscout = harness.addToBattlefieldAndReturn(player1, new ViashinoSandscout());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard().getName()).isEqualTo("Viashino Sandscout");
        assertThat(trigger.getSourcePermanentId()).isEqualTo(sandscout.getId());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Viashino Sandscout");
        harness.assertInHand(player1, "Viashino Sandscout");
    }

    @Test
    @DisplayName("Triggers during an opponent's end step")
    void triggersDuringOpponentsEndStep() {
        Permanent sandscout = harness.addToBattlefieldAndReturn(player1, new ViashinoSandscout());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(sandscout.getId());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Viashino Sandscout");
        harness.assertInHand(player1, "Viashino Sandscout");
    }

    @Test
    @DisplayName("Does not return itself if it leaves the battlefield before the trigger resolves")
    void doesNotReturnIfItLeavesBeforeTriggerResolves() {
        Permanent sandscout = harness.addToBattlefieldAndReturn(player1, new ViashinoSandscout());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(sandscout);

        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Viashino Sandscout");
    }

    @Test
    @DisplayName("Returns to its owner's hand rather than its controller's hand")
    void returnsToOwnersHandUnderAnotherPlayersControl() {
        ViashinoSandscout card = new ViashinoSandscout();
        card.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, card);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Viashino Sandscout");
        harness.assertInHand(player1, "Viashino Sandscout");
        harness.assertNotInHand(player2, "Viashino Sandscout");
    }

    @Test
    @DisplayName("An old trigger does not return the same card after it re-enters")
    void doesNotReturnNewPermanentAfterReentry() {
        ViashinoSandscout card = new ViashinoSandscout();
        Permanent original = harness.addToBattlefieldAndReturn(player1, card);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(original);
        Permanent returned = harness.enterBattlefieldAndReturn(player1, card);
        assertThat(returned.getId()).isNotEqualTo(original.getId());

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Viashino Sandscout");
        harness.assertNotInHand(player1, "Viashino Sandscout");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering after the end step begins waits until the next end step")
    void enteringDuringEndStepDoesNotTriggerImmediately() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);

        harness.enterBattlefieldAndReturn(player1, new ViashinoSandscout());

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Viashino Sandscout");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Viashino Sandscout");
        harness.assertInHand(player1, "Viashino Sandscout");
    }
}

