package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArachnoformTest extends BaseCardTest {

    private Permanent enchant(Permanent host) {
        Permanent aura = new Permanent(new Arachnoform());
        aura.setAttachedTo(host.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }

    @Test
    @DisplayName("Enchanted creature gets +2/+2, reach, and all creature types")
    void enchantedCreatureGetsBoostReachAndAllCreatureTypes() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        enchant(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.CHANGELING)).isTrue();
    }

    @Test
    @DisplayName("Arachnoform's effects end when it leaves the battlefield")
    void effectsEndWhenAuraLeaves() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = enchant(bears);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.REACH)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.CHANGELING)).isFalse();
    }

    @Test
    @DisplayName("Arachnoform can enchant only a creature")
    void cannotEnchantNoncreature() {
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new Arachnoform()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        Permanent mountain = findPermanent(player1, "Mountain");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
