package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GlitteringFrostTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted land becomes snow")
    void enchantedLandBecomesSnow() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GlitteringFrost());
        aura.setAttachedTo(forest.getId());

        assertThat(gqs.hasEffectiveSupertype(gd, forest, CardSupertype.SNOW)).isTrue();
    }

    @Test
    @DisplayName("Tapping enchanted land adds one mana of a chosen color")
    void tappingEnchantedLandAddsAnyColorMana() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GlitteringFrost());
        aura.setAttachedTo(forest.getId());

        harness.tapPermanent(player1, 0);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Snow effect ends when Glittering Frost leaves the battlefield")
    void snowEffectEndsWhenAuraLeaves() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GlitteringFrost());
        aura.setAttachedTo(forest.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.hasEffectiveSupertype(gd, forest, CardSupertype.SNOW)).isFalse();
    }
}
