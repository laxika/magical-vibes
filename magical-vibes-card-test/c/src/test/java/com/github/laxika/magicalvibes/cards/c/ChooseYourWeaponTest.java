package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.ArboreaPegasus;
import com.github.laxika.magicalvibes.cards.d.DireWolfProwler;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChooseYourWeapon.class, ArboreaPegasus.class, DireWolfProwler.class, Mountain.class})
class ChooseYourWeaponTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles target creature's power and toughness until end of turn")
    void doublesTargetCreaturePowerAndToughness() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DireWolfProwler());

        cast(0, target);

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("The doubling wears off at end of turn")
    void doublingWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DireWolfProwler());

        cast(0, target);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deals 5 damage to target creature with flying")
    void dealsFiveDamageToFlyingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArboreaPegasus());

        cast(1, target);

        harness.assertNotOnBattlefield(player2, "Arborea Pegasus");
        harness.assertInGraveyard(player2, "Arborea Pegasus");
    }

    @Test
    @DisplayName("The doubling mode cannot target a noncreature permanent")
    void doublingModeCannotTargetNoncreature() {
        harness.addToBattlefield(player1, new DireWolfProwler());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Mountain());

        assertThatThrownBy(() -> cast(0, target))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The Archery mode cannot target a creature without flying")
    void archeryModeCannotTargetCreatureWithoutFlying() {
        harness.addToBattlefield(player1, new ArboreaPegasus());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DireWolfProwler());

        assertThatThrownBy(() -> cast(1, target))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doublesOpponentsPowerAndToughnessIndependently() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArboreaPegasus());

        cast(0, target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(6);
    }

    @Test
    void successiveDoublingsUseCurrentStats() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DireWolfProwler());

        cast(0, target);
        cast(0, target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(8);
    }

    @Test
    void doublingIncludesCountersButDoesNotDoubleLaterCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DireWolfProwler());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        cast(0, target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(6);
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(7);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    void doublingUsesStatsAtResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DireWolfProwler());
        harness.setHand(player1, List.of(new ChooseYourWeapon()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castInstant(player1, 0, 0, target.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(8);
    }

    @Test
    void archeryDealsExactlyFiveDamageAndCanTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ArboreaPegasus());
        cast(0, target);

        cast(1, target);

        harness.assertOnBattlefield(player1, "Arborea Pegasus");
        assertThat(target.getMarkedDamage()).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(6);
    }

    @Test
    void archeryDoesNotResolveIfTargetLosesFlying() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArboreaPegasus());
        harness.setHand(player1, List.of(new ChooseYourWeapon()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castInstant(player1, 0, 1, target.getId());
        target.getRemovedKeywords().add(Keyword.FLYING);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Arborea Pegasus");
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Choose Your Weapon");
    }

    private void cast(int mode, Permanent target) {
        harness.setHand(player1, List.of(new ChooseYourWeapon()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castInstant(player1, 0, mode, target.getId());
        harness.passBothPriorities();
    }
}
