package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrinebarrowIntruder.class, GrizzlyBears.class, HillGiant.class})
class BrinebarrowIntruderTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives the targeted opponent creature -2/-0")
    void etbWeakensOpponentCreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new HillGiant());
        UUID targetId = harness.getPermanentId(player2, "Hill Giant");

        castIntruder(targetId);
        resolveAllTriggers();

        Permanent giant = findPermanent(player2, "Hill Giant");
        assertThat(giant.getEffectivePower()).isEqualTo(1);
        assertThat(giant.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("The power reduction wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new HillGiant());
        UUID targetId = harness.getPermanentId(player2, "Hill Giant");

        castIntruder(targetId);
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent giant = findPermanent(player2, "Hill Giant");
        assertThat(giant.getEffectivePower()).isEqualTo(3);
        assertThat(giant.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a creature you control")
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new BrinebarrowIntruder()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, targetId, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flash allows casting during the opponent's turn and power can become negative")
    void flashWeakensCreatureDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BrinebarrowIntruder());

        castIntruder(target.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Brinebarrow Intruder");
        assertThat(target.getEffectivePower()).isEqualTo(-1);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Brinebarrow Intruder");
    }

    @Test
    @DisplayName("Can enter when no opponent controls a creature")
    void canEnterWithoutLegalTargets() {
        castIntruder(null);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Brinebarrow Intruder");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering without being cast still triggers and rejects an own creature")
    void nonCastEntryChoosesOnlyOpponentCreature() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new BrinebarrowIntruder());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new BrinebarrowIntruder());

        harness.enterBattlefieldAndReturn(player1, new BrinebarrowIntruder());
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, own.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, opponent.getId());
        resolveAllTriggers();

        assertThat(own.getEffectivePower()).isEqualTo(1);
        assertThat(opponent.getEffectivePower()).isEqualTo(-1);
        assertThat(opponent.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("A departed target does not redirect the reduction to another creature")
    void departedTargetDoesNotWeakenAnotherCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BrinebarrowIntruder());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new BrinebarrowIntruder());

        castIntruder(target.getId());
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, target));
        resolveAllTriggers();

        assertThat(other.getEffectivePower()).isEqualTo(1);
        assertThat(other.getEffectiveToughness()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Brinebarrow Intruder");
        harness.assertInGraveyard(player2, "Brinebarrow Intruder");
    }

    private void castIntruder(UUID targetId) {
        harness.setHand(player1, List.of(new BrinebarrowIntruder()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0, 0, targetId);
    }
}
