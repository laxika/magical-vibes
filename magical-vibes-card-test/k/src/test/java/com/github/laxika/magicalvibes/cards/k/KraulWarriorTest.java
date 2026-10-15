package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KraulWarrior.class})
class KraulWarriorTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability gives +3/+3")
    void abilityBoostsSelf() {
        Permanent warrior = addCreatureReady(player1, new KraulWarrior());
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(warrior.getEffectivePower()).isEqualTo(5);
        assertThat(warrior.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Ability can be activated repeatedly")
    void abilityStacks() {
        Permanent warrior = addCreatureReady(player1, new KraulWarrior());
        harness.addMana(player1, ManaColor.GREEN, 12);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(warrior.getEffectivePower()).isEqualTo(8);
        assertThat(warrior.getEffectiveToughness()).isEqualTo(8);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent warrior = addCreatureReady(player1, new KraulWarrior());
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(warrior.getPowerModifier()).isEqualTo(0);
        assertThat(warrior.getToughnessModifier()).isEqualTo(0);
        assertThat(warrior.getEffectivePower()).isEqualTo(2);
        assertThat(warrior.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new KraulWarrior());
        harness.addMana(player1, ManaColor.GREEN, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Generic activation cost can be paid with colorless mana")
    void acceptsMixedManaPayment() {
        Permanent warrior = addCreatureReady(player1, new KraulWarrior());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(warrior.getEffectivePower()).isEqualTo(5);
        assertThat(warrior.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Tapped and summoning-sick warrior can activate its ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new KraulWarrior());
        warrior.setSummoningSick(true);
        warrior.tap();
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(warrior.getEffectivePower()).isEqualTo(5);
        assertThat(warrior.getEffectiveToughness()).isEqualTo(5);
        assertThat(warrior.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activation boosts only its source and waits for resolution")
    void boostsOnlySourceOnResolution() {
        Permanent warrior = addCreatureReady(player1, new KraulWarrior());
        Permanent otherWarrior = addCreatureReady(player1, new KraulWarrior());
        Permanent opposingWarrior = addCreatureReady(player2, new KraulWarrior());
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, null, null);

        assertThat(warrior.getPowerModifier()).isZero();
        assertThat(warrior.getToughnessModifier()).isZero();

        harness.passBothPriorities();

        assertThat(warrior.getEffectivePower()).isEqualTo(5);
        assertThat(warrior.getEffectiveToughness()).isEqualTo(5);
        assertThat(otherWarrior.getPowerModifier()).isZero();
        assertThat(otherWarrior.getToughnessModifier()).isZero();
        assertThat(opposingWarrior.getPowerModifier()).isZero();
        assertThat(opposingWarrior.getToughnessModifier()).isZero();
    }
}
