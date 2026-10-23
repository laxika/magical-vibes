package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExponentialGrowth.class, GrizzlyBears.class, FountainOfYouth.class, GiantGrowth.class})
class ExponentialGrowthTest extends BaseCardTest {

    @Test
    void doesNothingWithZeroX() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(target, 0, 2, 0);

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void doublesPowerOnceWithOneX() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(target, 1, 2, 2);

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void doublesPowerRepeatedlyWithTwoX() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(target, 2, 2, 4);

        assertThat(target.getEffectivePower()).isEqualTo(8);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void boostWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(target, 1, 2, 2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new ExponentialGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doublesNegativePowerWithoutChangingToughness() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setPowerModifier(-3);

        cast(target, 2, 2, 4);

        assertThat(target.getEffectivePower()).isEqualTo(-4);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void zeroPowerRemainsZero() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setPowerModifier(-2);

        cast(target, 3, 2, 6);

        assertThat(target.getEffectivePower()).isZero();
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void usesPowerAtResolutionAndDoesNotRedoubleLaterBoosts() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ExponentialGrowth(), new GiantGrowth(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, 2, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(20);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getEffectivePower()).isEqualTo(23);
        assertThat(target.getEffectiveToughness()).isEqualTo(8);
    }

    @Test
    void bothXSymbolsMustBePaid() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ExponentialGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void largeXSaturatesPowerInsteadOfWrappingNegative() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(target, 40, 2, 80);

        assertThat(target.getEffectivePower()).isEqualTo(Integer.MAX_VALUE);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void largeXSaturatesNegativePowerInsteadOfWrappingPositive() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setPowerModifier(-3);

        cast(target, 40, 2, 80);

        // The power modifier saturates at Integer.MIN_VALUE; the Bears' base power of 2 still adds on top.
        assertThat(target.getEffectivePower()).isEqualTo(Integer.MIN_VALUE + 2);
    }

    @Test
    void saturatedAttackerDealsSaturatedCombatDamage() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        cast(bears, 40, 2, 80);

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20 - Integer.MAX_VALUE);
    }

    private void cast(Permanent target, int x, int greenMana, int colorlessMana) {
        harness.setHand(player1, List.of(new ExponentialGrowth()));
        harness.addMana(player1, ManaColor.GREEN, greenMana);
        harness.addMana(player1, ManaColor.COLORLESS, colorlessMana);

        harness.castAndResolveSorcery(player1, 0, x, target.getId());
    }
}
