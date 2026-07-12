package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SilverskinArmor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EnchantedEveningTest extends BaseCardTest {

    @Test
    @DisplayName("All permanents become enchantments in addition to their other types")
    void allPermanentsBecomeEnchantments() {
        Permanent bears = new Permanent(new GrizzlyBears());
        Permanent forest = new Permanent(new Forest());
        Permanent armor = new Permanent(new SilverskinArmor());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        gd.playerBattlefields.get(player1.getId()).add(forest);
        gd.playerBattlefields.get(player2.getId()).add(armor);

        // Before Enchanted Evening, none of these are enchantments
        assertThat(gqs.isEnchantment(gd, bears)).isFalse();
        assertThat(gqs.isEnchantment(gd, forest)).isFalse();
        assertThat(gqs.isEnchantment(gd, armor)).isFalse();

        Permanent evening = new Permanent(new EnchantedEvening());
        gd.playerBattlefields.get(player1.getId()).add(evening);

        // Every permanent on the battlefield is now an enchantment, regardless of controller
        assertThat(gqs.isEnchantment(gd, bears)).isTrue();
        assertThat(gqs.isEnchantment(gd, forest)).isTrue();
        assertThat(gqs.isEnchantment(gd, armor)).isTrue();
        // Enchanted Evening itself is an enchantment (natively)
        assertThat(gqs.isEnchantment(gd, evening)).isTrue();
    }

    @Test
    @DisplayName("Grant disappears when Enchanted Evening leaves the battlefield")
    void grantDisappearsWhenEveningLeaves() {
        Permanent bears = new Permanent(new GrizzlyBears());
        Permanent evening = new Permanent(new EnchantedEvening());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        gd.playerBattlefields.get(player1.getId()).add(evening);

        assertThat(gqs.isEnchantment(gd, bears)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(evening);

        assertThat(gqs.isEnchantment(gd, bears)).isFalse();
    }
}
