package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.Abundance;
import com.github.laxika.magicalvibes.cards.g.GreaterGood;
import com.github.laxika.magicalvibes.cards.w.WornPowerstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SerrasSanctum.class, Abundance.class, GreaterGood.class, WornPowerstone.class})
class SerrasSanctumTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds one white mana for each enchantment you control")
    void tappingAddsManaForControlledEnchantments() {
        Permanent sanctum = harness.addToBattlefieldAndReturn(player1, new SerrasSanctum());
        harness.addToBattlefield(player1, new Abundance());
        harness.addToBattlefield(player1, new GreaterGood());
        harness.addToBattlefield(player1, new WornPowerstone());
        harness.addToBattlefield(player2, new Abundance());

        harness.activateAbility(player1, 0, null, null);

        assertThat(sanctum.isTapped()).isTrue();
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
