package com.github.laxika.magicalvibes.cards.v;

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

class VengefulRebelTest extends BaseCardTest {

    @Test
    @DisplayName("Revolt ETB gives an opponent's creature -3/-3")
    void revoltShrinksOpponentCreature() {
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, ownBears));
        GrizzlyBears opponentCreature = new GrizzlyBears();
        opponentCreature.setPower(5);
        opponentCreature.setToughness(5);
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, opponentCreature);

        castRebel();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, opponentBears.getId());
        harness.passBothPriorities();

        assertThat(opponentBears.getEffectivePower()).isEqualTo(2);
        assertThat(opponentBears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Without Revolt, the ETB ability does not trigger")
    void doesNotTriggerWithoutRevolt() {
        GrizzlyBears opponentCreature = new GrizzlyBears();
        opponentCreature.setPower(5);
        opponentCreature.setToughness(5);
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, opponentCreature);

        castRebel();
        harness.passBothPriorities();

        assertThat(opponentBears.getEffectivePower()).isEqualTo(5);
        assertThat(opponentBears.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("The Revolt trigger only offers opposing creatures")
    void triggerOnlyOffersOpposingCreatures() {
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        GrizzlyBears opponentCreature = new GrizzlyBears();
        opponentCreature.setPower(5);
        opponentCreature.setToughness(5);
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, opponentCreature);
        Permanent ownLeavingPermanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, ownLeavingPermanent));

        castRebel();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(opponentBears.getId());
        assertThat(choice.validIds()).doesNotContain(ownBears.getId());
    }

    @Test
    @DisplayName("The Revolt debuff wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, ownBears));
        GrizzlyBears opponentCreature = new GrizzlyBears();
        opponentCreature.setPower(5);
        opponentCreature.setToughness(5);
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, opponentCreature);

        castRebel();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, opponentBears.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(opponentBears.getEffectivePower()).isEqualTo(5);
        assertThat(opponentBears.getEffectiveToughness()).isEqualTo(5);
    }

    private void castRebel() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new VengefulRebel()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }
}
