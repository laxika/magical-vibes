package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UlvenwaldCaptive.class, UlvenwaldAbomination.class})
class UlvenwaldCaptiveTest extends BaseCardTest {

    @Test
    @DisplayName("Ulvenwald Captive taps for one green mana")
    void frontFaceAddsGreenMana() {
        addCreatureReady(player1, new UlvenwaldCaptive());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ulvenwald Captive transforms when its ability is activated")
    void transformsIntoUlvenwaldAbomination() {
        Permanent captive = addCreatureReady(player1, new UlvenwaldCaptive());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(captive.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Ulvenwald Abomination taps for two colorless mana")
    void backFaceAddsTwoColorlessMana() {
        Permanent captive = addCreatureReady(player1, new UlvenwaldCaptive());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    @DisplayName("The mana ability taps its source and resolves without using the stack")
    void manaAbilityResolvesImmediatelyAndCannotBeUsedWhileTapped() {
        Permanent captive = addCreatureReady(player1, new UlvenwaldCaptive());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(captive.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("A tapped Captive can transform without untapping")
    void transformsWhileTappedAndCanUseItsOwnManaToPay() {
        Permanent captive = addCreatureReady(player1, new UlvenwaldCaptive());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, 0, null, null);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(captive.isTransformed()).isFalse();
        assertThat(captive.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.passBothPriorities();

        assertThat(captive.isTransformed()).isTrue();
        assertThat(captive.getCard()).isInstanceOf(UlvenwaldAbomination.class);
        assertThat(captive.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Transforming does not bypass summoning sickness for the mana ability")
    void summoningSickCreatureCanTransformButCannotTapForMana() {
        Permanent captive = harness.addToBattlefieldAndReturn(player1, new UlvenwaldCaptive());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(captive.isTransformed()).isTrue();
        assertThat(captive.isTapped()).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("The transform cost requires two green mana")
    void cannotTransformWithOnlyOneGreenMana() {
        Permanent captive = addCreatureReady(player1, new UlvenwaldCaptive());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(captive.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Queued transform activations leave the creature on its back face")
    void multipleQueuedActivationsDoNotTransformBack() {
        Permanent captive = addCreatureReady(player1, new UlvenwaldCaptive());
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        assertThat(captive.isTransformed()).isTrue();
        harness.passBothPriorities();

        assertThat(captive.isTransformed()).isTrue();
        assertThat(captive.getCard()).isInstanceOf(UlvenwaldAbomination.class);
        assertThat(gd.stack).isEmpty();
    }
}
