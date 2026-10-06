package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RavingOniSlave.class, RazorjawOni.class})
class RavingOniSlaveTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield makes its controller lose 3 life without a Demon")
    void enteringWithoutDemonCausesLifeLoss() {
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new RavingOniSlave(), "{1}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Entering the battlefield causes no life loss while its controller controls a Demon")
    void enteringWithDemonCausesNoLifeLoss() {
        harness.addToBattlefield(player1, new RazorjawOni());
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new RavingOniSlave(), "{1}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Leaving the battlefield makes its controller lose 3 life without a Demon")
    void leavingWithoutDemonCausesLifeLoss() {
        Permanent slave = harness.addToBattlefieldAndReturn(player1, new RavingOniSlave());
        harness.setLife(player1, 20);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, slave));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Leaving the battlefield causes no life loss while its controller controls a Demon")
    void leavingWithDemonCausesNoLifeLoss() {
        Permanent slave = harness.addToBattlefieldAndReturn(player1, new RavingOniSlave());
        harness.addToBattlefield(player1, new RazorjawOni());
        harness.setLife(player1, 20);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, slave));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Entering the battlefield causes no life loss when a Demon appears before the trigger resolves")
    void enteringWithDemonAddedBeforeTriggerResolvesCausesNoLifeLoss() {
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new RavingOniSlave(), "{1}{B}");
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new RazorjawOni());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Leaving the battlefield causes no life loss when a Demon appears before the trigger resolves")
    void leavingWithDemonAddedBeforeTriggerResolvesCausesNoLifeLoss() {
        Permanent slave = harness.addToBattlefieldAndReturn(player1, new RavingOniSlave());
        harness.setLife(player1, 20);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, slave));
        harness.addToBattlefield(player1, new RazorjawOni());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An opponent's Demon does not prevent the entering life loss")
    void opponentsDemonDoesNotPreventEnteringLifeLoss() {
        harness.addToBattlefield(player2, new RazorjawOni());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new RavingOniSlave(), "{1}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Entering still triggers with a Demon, and loses life if that Demon leaves before resolution")
    void enteringWithDemonRemovedBeforeResolutionCausesLifeLoss() {
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new RazorjawOni());
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new RavingOniSlave(), "{1}{B}");
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, demon));
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Leaving still triggers with a Demon, and loses life if that Demon leaves before resolution")
    void leavingWithDemonRemovedBeforeResolutionCausesLifeLoss() {
        Permanent slave = harness.addToBattlefieldAndReturn(player1, new RavingOniSlave());
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new RazorjawOni());
        harness.setLife(player1, 20);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, slave));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, demon));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Exile triggers the leaving life loss just like death")
    void exileCausesLeavingLifeLoss() {
        Permanent slave = harness.addToBattlefieldAndReturn(player1, new RavingOniSlave());
        harness.setLife(player1, 20);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, slave));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
    }
}
