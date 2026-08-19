package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeadeyeHarpoonerTest extends BaseCardTest {

    @Test
    @DisplayName("Revolt ETB destroys a tapped creature an opponent controls")
    void revoltDestroysTappedOpponentCreature() {
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, ownBears));
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opponentBears.tap();

        castDeadeyeHarpooner();
        harness.handlePermanentChosen(player1, opponentBears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Without Revolt, the ETB ability does not trigger")
    void doesNotTriggerWithoutRevolt() {
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opponentBears.tap();

        castDeadeyeHarpooner();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The Revolt trigger only offers tapped opposing creatures")
    void triggerOnlyOffersTappedOpposingCreatures() {
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        ownBears.tap();
        Permanent opponentUntappedBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent opponentTappedBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opponentTappedBears.tap();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, ownBears));

        castDeadeyeHarpooner();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactly(opponentTappedBears.getId());
        assertThat(choice.validIds()).doesNotContain(opponentUntappedBears.getId());
    }

    @Test
    @DisplayName("A permanent leaving under an opponent's control does not enable Revolt")
    void opponentPermanentLeavingDoesNotEnableRevolt() {
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, opponentBears));
        Permanent remainingOpponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        remainingOpponentBears.tap();

        castDeadeyeHarpooner();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    private void castDeadeyeHarpooner() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new DeadeyeHarpooner()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
