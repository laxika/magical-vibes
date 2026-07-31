package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AwakenTheAncientTest extends BaseCardTest {

    private Permanent enchant(Permanent land) {
        Permanent aura = new Permanent(new AwakenTheAncient());
        aura.setAttachedTo(land.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }

    @Test
    @DisplayName("Enchanted Mountain is a 7/7 red Giant creature with haste")
    void enchantedMountainBecomesCreature() {
        harness.addToBattlefield(player1, new Mountain());
        Permanent mountain = gd.playerBattlefields.get(player1.getId()).getFirst();
        enchant(mountain);

        assertThat(gqs.isCreature(gd, mountain)).isTrue();
        assertThat(gqs.getEffectivePower(gd, mountain)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, mountain)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, mountain, Keyword.HASTE)).isTrue();

        GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, mountain);
        assertThat(bonus.animatedCreature()).isTrue();
        assertThat(bonus.grantedColors()).contains(CardColor.RED);
        assertThat(bonus.grantedSubtypes()).contains(CardSubtype.GIANT);
        assertThat(bonus.grantedCardTypes()).contains(CardType.CREATURE);
    }

    @Test
    @DisplayName("Enchanted Mountain is still a land and taps for red mana")
    void enchantedMountainStillTapsForMana() {
        harness.addToBattlefield(player1, new Mountain());
        Permanent mountain = gd.playerBattlefields.get(player1.getId()).getFirst();
        enchant(mountain);

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gqs.isCreature(gd, mountain)).isTrue();
    }

    @Test
    @DisplayName("Mountain reverts when the Aura leaves the battlefield")
    void mountainRevertsWhenAuraLeaves() {
        harness.addToBattlefield(player1, new Mountain());
        Permanent mountain = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent aura = enchant(mountain);

        assertThat(gqs.isCreature(gd, mountain)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.isCreature(gd, mountain)).isFalse();
        assertThat(gqs.hasKeyword(gd, mountain, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Cannot cast Awaken the Ancient targeting a non-Mountain land")
    void cannotTargetNonMountain() {
        harness.addToBattlefield(player1, new Mountain()); // valid target so the spell is playable
        harness.addToBattlefield(player1, new Forest());
        Permanent forest = gd.playerBattlefields.get(player1.getId()).get(1);
        harness.setHand(player1, List.of(new AwakenTheAncient()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Mountain");
    }
}
