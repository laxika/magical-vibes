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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SanctumWeaver.class, Abundance.class, GreaterGood.class, WornPowerstone.class})
class SanctumWeaverTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds one mana of the chosen color for each enchantment you control")
    void tappingAddsManaForControlledEnchantments() {
        addCreatureReady(player1, new SanctumWeaver());
        harness.addToBattlefield(player1, new Abundance());
        harness.addToBattlefield(player1, new GreaterGood());
        harness.addToBattlefield(player1, new WornPowerstone());
        harness.addToBattlefield(player2, new Abundance());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Tapping counts Sanctum Weaver as a controlled enchantment")
    void tappingCountsItselfAsAControlledEnchantment() {
        addCreatureReady(player1, new SanctumWeaver());
        harness.addToBattlefield(player1, new WornPowerstone());
        harness.addToBattlefield(player2, new Abundance());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("A summoning-sick Sanctum Weaver cannot activate")
    void cannotActivateWithSummoningSickness() {
        Permanent weaver = harness.addToBattlefieldAndReturn(player1, new SanctumWeaver());

        assertThat(weaver.isSummoningSick()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }
}
