package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
}
