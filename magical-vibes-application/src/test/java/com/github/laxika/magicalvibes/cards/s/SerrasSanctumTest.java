package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AuraOfSilence;
import com.github.laxika.magicalvibes.cards.e.ElephantGrass;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SerrasSanctumTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds one white mana for each enchantment you control")
    void tappingAddsManaForControlledEnchantments() {
        harness.addToBattlefield(player1, new SerrasSanctum());
        harness.addToBattlefield(player1, new ElephantGrass());
        harness.addToBattlefield(player1, new AuraOfSilence());
        harness.addToBattlefield(player1, new Millstone());
        harness.addToBattlefield(player2, new ElephantGrass());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Tapping produces no mana without a controlled enchantment")
    void tappingProducesNoManaWithoutControlledEnchantments() {
        harness.addToBattlefield(player1, new SerrasSanctum());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
