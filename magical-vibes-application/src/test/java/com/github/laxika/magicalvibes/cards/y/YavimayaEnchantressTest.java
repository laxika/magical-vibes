package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.t.Telepathy;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrizzlyBears.class, HolyStrength.class, Telepathy.class, YavimayaEnchantress.class})
class YavimayaEnchantressTest extends BaseCardTest {

    @Test
    @DisplayName("Base stats are 2/2 with no enchantments on battlefield")
    void baseStatsWithNoEnchantments() {
        Permanent enchantress = harness.addToBattlefieldAndReturn(player1, new YavimayaEnchantress());

        assertThat(gqs.getEffectivePower(gd, enchantress)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enchantress)).isEqualTo(2);
    }

    @Test
    @DisplayName("Enchantments outside the battlefield do not boost")
    void enchantmentsOutsideBattlefieldDoNotBoost() {
        Permanent enchantress = harness.addToBattlefieldAndReturn(player1, new YavimayaEnchantress());
        harness.setGraveyard(player1, List.of(new Telepathy()));

        assertThat(gqs.getEffectivePower(gd, enchantress)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enchantress)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gets +1/+1 for own enchantment on battlefield")
    void boostedByOwnEnchantment() {
        Permanent enchantress = harness.addToBattlefieldAndReturn(player1, new YavimayaEnchantress());
        harness.addToBattlefield(player1, new Telepathy());

        assertThat(gqs.getEffectivePower(gd, enchantress)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enchantress)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gets +1/+1 for own enchantment on battlefield")
    void boostedByOwnEnchantmentUpstreamReview() {
        Permanent enchantress = harness.addToBattlefieldAndReturn(player1, new YavimayaEnchantress());
        harness.addToBattlefield(player1, new Telepathy());

        assertThat(gqs.getEffectivePower(gd, enchantress)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enchantress)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gets +1/+1 for opponent's enchantment on battlefield")
    void boostedByOpponentEnchantment() {
        Permanent enchantress = harness.addToBattlefieldAndReturn(player1, new YavimayaEnchantress());
        harness.addToBattlefield(player2, new Telepathy());

        assertThat(gqs.getEffectivePower(gd, enchantress)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enchantress)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gets +1/+1 for opponent's enchantment on battlefield")
    void boostedByOpponentEnchantmentUpstreamReview() {
        Permanent enchantress = harness.addToBattlefieldAndReturn(player1, new YavimayaEnchantress());
        harness.addToBattlefield(player2, new Telepathy());

        assertThat(gqs.getEffectivePower(gd, enchantress)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enchantress)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gets +1/+1 for each enchantment, stacks with multiple")
    void boostedByMultipleEnchantments() {
        Permanent enchantress = harness.addToBattlefieldAndReturn(player1, new YavimayaEnchantress());
        harness.addToBattlefield(player1, new Telepathy());
        harness.addToBattlefield(player2, new Telepathy());

        assertThat(gqs.getEffectivePower(gd, enchantress)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, enchantress)).isEqualTo(4);
    }

    @Test
    @DisplayName("Gets +1/+1 for each enchantment, stacks with multiple")
    void boostedByMultipleEnchantmentsUpstreamReview() {
        Permanent enchantress = harness.addToBattlefieldAndReturn(player1, new YavimayaEnchantress());
        harness.addToBattlefield(player1, new Telepathy());
        harness.addToBattlefield(player2, new Telepathy());

        assertThat(gqs.getEffectivePower(gd, enchantress)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, enchantress)).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost updates when enchantment is removed")
    void boostUpdatesWhenEnchantmentRemoved() {
        Permanent enchantress = harness.addToBattlefieldAndReturn(player1, new YavimayaEnchantress());
        harness.addToBattlefield(player1, new Telepathy());

        assertThat(gqs.getEffectivePower(gd, enchantress)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enchantress)).isEqualTo(3);

        // Remove the enchantment
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Telepathy"));

        assertThat(gqs.getEffectivePower(gd, enchantress)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enchantress)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost updates when enchantment is removed")
    void boostUpdatesWhenEnchantmentRemovedUpstreamReview() {
        Permanent enchantress = harness.addToBattlefieldAndReturn(player1, new YavimayaEnchantress());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new Telepathy());

        assertThat(gqs.getEffectivePower(gd, enchantress)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enchantress)).isEqualTo(3);

        // Remove the enchantment
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, enchantment));

        assertThat(gqs.getEffectivePower(gd, enchantress)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enchantress)).isEqualTo(2);
    }

    @Test
    @DisplayName("Non-enchantment permanents do not boost")
    void nonEnchantmentDoesNotBoost() {
        Permanent enchantress = harness.addToBattlefieldAndReturn(player1, new YavimayaEnchantress());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, enchantress)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enchantress)).isEqualTo(2);
    }

    @Test
    @DisplayName("Auras on the battlefield also count as enchantments")
    void aurasCountAsEnchantments() {
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent enchantress = harness.addToBattlefieldAndReturn(player1, new YavimayaEnchantress());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        aura.setAttachedTo(spider.getId());

        assertThat(gqs.getEffectivePower(gd, enchantress)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enchantress)).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost updates only when an enchantment spell resolves")
    void boostUpdatesWhenEnchantmentResolves() {
        Permanent enchantress = harness.addToBattlefieldAndReturn(player1, new YavimayaEnchantress());
        harness.setHand(player1, List.of(new Telepathy()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThat(gqs.getEffectivePower(gd, enchantress)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enchantress)).isEqualTo(2);
        harness.castEnchantment(player1, 0);
        assertThat(gqs.getEffectivePower(gd, enchantress)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enchantress)).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, enchantress)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enchantress)).isEqualTo(3);
    }

    @Test
    @DisplayName("An Aura enchanting the Enchantress contributes both its own bonus and the enchantment count")
    void attachedAuraBonusStacksWithEnchantmentCount() {
        Permanent enchantress = harness.addToBattlefieldAndReturn(player1, new YavimayaEnchantress());
        harness.setHand(player1, List.of(new HolyStrength()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, enchantress.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, enchantress)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, enchantress)).isEqualTo(5);
    }
}
