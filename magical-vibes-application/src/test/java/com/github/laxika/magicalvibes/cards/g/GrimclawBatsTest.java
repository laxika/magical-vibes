package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(GrimclawBats.class)
class GrimclawBatsTest extends BaseCardTest {

    @Test
    @DisplayName("Ability gives Grimclaw Bats +1/+1 and costs 1 life")
    void abilityBoostsAndCostsLife() {
        Permanent bats = addCreatureReady(player1, new GrimclawBats());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bats)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bats)).isEqualTo(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Ability does not tap Grimclaw Bats")
    void abilityDoesNotTapSource() {
        Permanent bats = addCreatureReady(player1, new GrimclawBats());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bats.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Ability requires black mana")
    void abilityRequiresBlackMana() {
        addCreatureReady(player1, new GrimclawBats());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Ability can be activated multiple times")
    void abilityStacksBoost() {
        Permanent bats = addCreatureReady(player1, new GrimclawBats());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bats)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bats)).isEqualTo(3);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Cannot activate the ability without enough life")
    void cannotActivateWithInsufficientLife() {
        addCreatureReady(player1, new GrimclawBats());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent bats = addCreatureReady(player1, new GrimclawBats());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bats)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bats)).isEqualTo(1);
    }

    @Test
    @DisplayName("Life is paid immediately and the boost waits for resolution")
    void lifeIsPaidBeforeResolution() {
        Permanent bats = addCreatureReady(player1, new GrimclawBats());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gqs.getEffectivePower(gd, bats)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bats)).isEqualTo(1);

        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gqs.getEffectivePower(gd, bats)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bats)).isEqualTo(2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Tapped and summoning-sick Grimclaw Bats can activate the ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent bats = harness.addToBattlefieldAndReturn(player1, new GrimclawBats());
        bats.setSummoningSick(true);
        bats.tap();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bats)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bats)).isEqualTo(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(bats.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The boost affects only the copy that activated the ability")
    void boostsOnlyItsSource() {
        Permanent first = addCreatureReady(player1, new GrimclawBats());
        Permanent second = addCreatureReady(player1, new GrimclawBats());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
    }
}
