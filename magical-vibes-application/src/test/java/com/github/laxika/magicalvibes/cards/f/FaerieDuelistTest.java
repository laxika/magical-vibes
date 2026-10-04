package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({FaerieDuelist.class, GrizzlyBears.class})
class FaerieDuelistTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives target creature an opponent controls -2/-0")
    void etbWeakensTargetOpponentCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castFaerieDuelist(bears.getId());

        resolveAllTriggers();

        assertThat(bears.getPowerModifier()).isEqualTo(-2);
        assertThat(bears.getToughnessModifier()).isZero();
        assertThat(bears.getEffectivePower()).isZero();
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a creature its controller controls")
    void cannotTargetOwnCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FaerieDuelist()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, bears.getId(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The -2/-0 wears off at end of turn")
    void weakeningWearsOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castFaerieDuelist(bears.getId());

        resolveAllTriggers();
        assertThat(bears.getPowerModifier()).isEqualTo(-2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Can be cast when no opponent creature is available")
    void castWithoutOpponentCreature() {
        harness.setHand(player1, List.of(new FaerieDuelist()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Faerie Duelist");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Flash allows casting during an opponent's turn")
    void canCastDuringOpponentTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();

        castFaerieDuelist(bears.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Faerie Duelist");
        assertThat(bears.getPowerModifier()).isEqualTo(-2);
        assertThat(bears.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Only the targeted opponent creature is weakened")
    void weakensOnlyTheChosenCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castFaerieDuelist(target.getId());
        resolveAllTriggers();

        assertThat(target.getPowerModifier()).isEqualTo(-2);
        assertThat(other.getPowerModifier()).isZero();
        assertThat(own.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("The triggered ability resolves even if Faerie Duelist leaves the battlefield")
    void triggerResolvesAfterSourceLeaves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castFaerieDuelist(bears.getId());
        harness.passBothPriorities();
        Permanent duelist = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Faerie Duelist"));
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, duelist);

        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Faerie Duelist");
        assertThat(bears.getPowerModifier()).isEqualTo(-2);
        assertThat(bears.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The triggered ability does not affect another creature if its target leaves")
    void triggerDoesNotRetargetWhenTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castFaerieDuelist(target.getId());
        harness.passBothPriorities();
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, target);

        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Faerie Duelist");
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void castFaerieDuelist(UUID targetId) {
        harness.setHand(player1, List.of(new FaerieDuelist()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0, 0, targetId);
    }
}
