package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.e.EverflowingChalice;
import com.github.laxika.magicalvibes.cards.w.WalkingAtlas;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BullRush.class, WalkingAtlas.class, EverflowingChalice.class})
class BullRushTest extends BaseCardTest {

    @Test
    void givesTargetCreaturePlusTwoPowerUntilEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WalkingAtlas());
        harness.setHand(player1, List.of(new BullRush()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    void boostWearsOffAtCleanup() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WalkingAtlas());
        harness.setHand(player1, List.of(new BullRush()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        harness.addToBattlefield(player1, new WalkingAtlas());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new EverflowingChalice());
        harness.setHand(player1, List.of(new BullRush()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void canBoostAnOpponentsCreatureWithoutBoostingOtherCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new WalkingAtlas());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WalkingAtlas());
        harness.setHand(player1, List.of(new BullRush()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
        assertThat(ownCreature.getPowerModifier()).isZero();
        assertThat(ownCreature.getToughnessModifier()).isZero();
    }

    @Test
    void repeatedBoostsAccumulateAndBothExpireAtCleanup() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WalkingAtlas());
        harness.setHand(player1, List.of(new BullRush(), new BullRush()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }
}
