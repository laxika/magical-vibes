package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SilverskinArmor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EnchantedEvening.class, Forest.class, GrizzlyBears.class, SilverskinArmor.class})
class EnchantedEveningTest extends BaseCardTest {

    @Test
    @DisplayName("All permanents become enchantments in addition to their other types")
    void allPermanentsBecomeEnchantments() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent armor = harness.addToBattlefieldAndReturn(player2, new SilverskinArmor());

        // Before Enchanted Evening, none of these are enchantments
        assertThat(gqs.isEnchantment(gd, bears)).isFalse();
        assertThat(gqs.isEnchantment(gd, forest)).isFalse();
        assertThat(gqs.isEnchantment(gd, armor)).isFalse();

        Permanent evening = harness.addToBattlefieldAndReturn(player1, new EnchantedEvening());

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
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent evening = harness.addToBattlefieldAndReturn(player1, new EnchantedEvening());

        assertThat(gqs.isEnchantment(gd, bears)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(evening);

        assertThat(gqs.isEnchantment(gd, bears)).isFalse();
    }

    @Test
    @DisplayName("Permanents entering later gain enchantment type and retain their other types")
    void laterPermanentsGainEnchantmentType() {
        harness.addToBattlefield(player1, new EnchantedEvening());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new SilverskinArmor());

        assertThat(gqs.isEnchantment(gd, bears)).isTrue();
        assertThat(gqs.isCreature(gd, bears)).isTrue();
        assertThat(gqs.isEnchantment(gd, forest)).isTrue();
        assertThat(gqs.isLand(gd, forest)).isTrue();
        assertThat(gqs.isEnchantment(gd, armor)).isTrue();
        assertThat(gqs.isArtifact(gd, armor)).isTrue();
    }

    @Test
    @DisplayName("Removing one of two copies leaves the other copy's effect active")
    void overlappingCopiesKeepGrantActive() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new EnchantedEvening());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new EnchantedEvening());

        assertThat(gqs.isEnchantment(gd, forest)).isTrue();
        gd.playerBattlefields.get(player1.getId()).remove(first);
        assertThat(gqs.isEnchantment(gd, forest)).isTrue();
        gd.playerBattlefields.get(player2.getId()).remove(second);
        assertThat(gqs.isEnchantment(gd, forest)).isFalse();
        assertThat(gqs.isLand(gd, forest)).isTrue();
    }
}
