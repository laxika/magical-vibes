package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(TowerDrake.class)
class TowerDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Ability can be activated while summoning sick")
    void abilityCanBeActivatedWhileSummoningSick() {
        Permanent drake = harness.addToBattlefieldAndReturn(player1, new TowerDrake());
        drake.setSummoningSick(true);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, drake)).isEqualTo(2);
        assertThat(drake.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Boost affects only its source and waits for resolution")
    void boostAffectsOnlySourceAfterResolution() {
        Permanent drake = addCreatureReady(player1, new TowerDrake());
        Permanent otherDrake = addCreatureReady(player1, new TowerDrake());
        Permanent opposingDrake = addCreatureReady(player2, new TowerDrake());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gqs.getEffectiveToughness(gd, drake)).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, drake)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherDrake)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposingDrake)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability grants +0/+1 without tapping")
    void abilityGrantsToughness() {
        Permanent drake = addCreatureReady(player1, new TowerDrake());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, drake)).isEqualTo(2);
        assertThat(drake.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Ability can be activated while the drake is tapped")
    void abilityCanBeActivatedWhileTapped() {
        Permanent drake = addCreatureReady(player1, new TowerDrake());
        drake.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, drake)).isEqualTo(2);
        assertThat(drake.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability can be activated multiple times")
    void abilityStacks() {
        Permanent drake = addCreatureReady(player1, new TowerDrake());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, drake)).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent drake = addCreatureReady(player1, new TowerDrake());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectiveToughness(gd, drake)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, drake)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability requires white mana")
    void cannotActivateWithoutWhiteMana() {
        addCreatureReady(player1, new TowerDrake());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }
}
