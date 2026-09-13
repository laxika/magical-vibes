package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.g.Greed;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HulkingOgre;
import com.github.laxika.magicalvibes.cards.m.MarkOfFury;
import com.github.laxika.magicalvibes.cards.m.MentalDiscipline;
import com.github.laxika.magicalvibes.cards.p.PlatedSpider;
import com.github.laxika.magicalvibes.cards.u.UnholyStrength;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Greed.class, GrizzlyBears.class, HulkingOgre.class, MarkOfFury.class, MentalDiscipline.class, PlatedSpider.class, UnholyStrength.class, YavimayaEnchantress.class})
class YavimayaEnchantressTest extends BaseCardTest {

    // ===== Static boost =====

    @Test
    @DisplayName("Base stats are 2/2 with no enchantments on battlefield")
    void baseStatsWithNoEnchantments() {
        harness.addToBattlefield(player1, new YavimayaEnchantress());

        Permanent enchantress = findPermanent(player1, "Yavimaya Enchantress");
        assertThat(gqs.getEffectivePower(gd, enchantress)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enchantress)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gets +1/+1 for own enchantment on battlefield")
    void boostedByOwnEnchantment() {
        harness.addToBattlefield(player1, new YavimayaEnchantress());
        harness.addToBattlefield(player1, new Greed());

        Permanent enchantress = findPermanent(player1, "Yavimaya Enchantress");
        assertThat(gqs.getEffectivePower(gd, enchantress)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enchantress)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gets +1/+1 for own enchantment on battlefield")
    void boostedByOwnEnchantmentUpstreamReview() {
        harness.addToBattlefield(player1, new YavimayaEnchantress());
        harness.addToBattlefield(player1, new MentalDiscipline());

        Permanent enchantress = findPermanent(player1, "Yavimaya Enchantress");
        assertThat(gqs.getEffectivePower(gd, enchantress)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enchantress)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gets +1/+1 for opponent's enchantment on battlefield")
    void boostedByOpponentEnchantment() {
        harness.addToBattlefield(player1, new YavimayaEnchantress());
        harness.addToBattlefield(player2, new Greed());

        Permanent enchantress = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, enchantress)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enchantress)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gets +1/+1 for opponent's enchantment on battlefield")
    void boostedByOpponentEnchantmentUpstreamReview() {
        harness.addToBattlefield(player1, new YavimayaEnchantress());
        harness.addToBattlefield(player2, new MentalDiscipline());

        Permanent enchantress = findPermanent(player1, "Yavimaya Enchantress");
        assertThat(gqs.getEffectivePower(gd, enchantress)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enchantress)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gets +1/+1 for each enchantment, stacks with multiple")
    void boostedByMultipleEnchantments() {
        harness.addToBattlefield(player1, new YavimayaEnchantress());
        harness.addToBattlefield(player1, new Greed());
        harness.addToBattlefield(player2, new Greed());

        Permanent enchantress = findPermanent(player1, "Yavimaya Enchantress");
        assertThat(gqs.getEffectivePower(gd, enchantress)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, enchantress)).isEqualTo(4);
    }

    @Test
    @DisplayName("Gets +1/+1 for each enchantment, stacks with multiple")
    void boostedByMultipleEnchantmentsUpstreamReview() {
        harness.addToBattlefield(player1, new YavimayaEnchantress());
        harness.addToBattlefield(player1, new MentalDiscipline());
        harness.addToBattlefield(player2, new MentalDiscipline());

        Permanent enchantress = findPermanent(player1, "Yavimaya Enchantress");
        assertThat(gqs.getEffectivePower(gd, enchantress)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, enchantress)).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost updates when enchantment is removed")
    void boostUpdatesWhenEnchantmentRemoved() {
        harness.addToBattlefield(player1, new YavimayaEnchantress());
        harness.addToBattlefield(player1, new Greed());

        Permanent enchantress = findPermanent(player1, "Yavimaya Enchantress");
        assertThat(gqs.getEffectivePower(gd, enchantress)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enchantress)).isEqualTo(3);

        // Remove the enchantment
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Greed"));

        assertThat(gqs.getEffectivePower(gd, enchantress)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enchantress)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost updates when enchantment is removed")
    void boostUpdatesWhenEnchantmentRemovedUpstreamReview() {
        harness.addToBattlefield(player1, new YavimayaEnchantress());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new MentalDiscipline());

        Permanent enchantress = findPermanent(player1, "Yavimaya Enchantress");
        assertThat(gqs.getEffectivePower(gd, enchantress)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enchantress)).isEqualTo(3);

        // Remove the enchantment
        gd.playerBattlefields.get(player1.getId()).remove(enchantment);

        assertThat(gqs.getEffectivePower(gd, enchantress)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enchantress)).isEqualTo(2);
    }

    @Test
    @DisplayName("Non-enchantment permanents do not boost")
    void nonEnchantmentDoesNotBoost() {
        harness.addToBattlefield(player1, new YavimayaEnchantress());
        harness.addToBattlefield(player1, new HulkingOgre());

        Permanent enchantress = findPermanent(player1, "Yavimaya Enchantress");
        assertThat(gqs.getEffectivePower(gd, enchantress)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enchantress)).isEqualTo(2);
    }

    @Test
    @DisplayName("Auras on the battlefield also count as enchantments")
    void aurasCountAsEnchantments() {
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new PlatedSpider());
        harness.addToBattlefield(player1, new YavimayaEnchantress());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new MarkOfFury());
        aura.setAttachedTo(spider.getId());

        Permanent enchantress = findPermanent(player1, "Yavimaya Enchantress");
        assertThat(gqs.getEffectivePower(gd, enchantress)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enchantress)).isEqualTo(3);
    }
}
