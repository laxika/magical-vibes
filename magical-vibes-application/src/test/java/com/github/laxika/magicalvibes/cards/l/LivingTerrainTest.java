package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LivingTerrainTest extends BaseCardTest {

    private Permanent enchant(Permanent land) {
        Permanent aura = new Permanent(new LivingTerrain());
        aura.setAttachedTo(land.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }

    @Test
    @DisplayName("Enchanted land is a 5/6 green Treefolk creature")
    void enchantedLandBecomesCreature() {
        harness.addToBattlefield(player1, new Forest());
        Permanent forest = gd.playerBattlefields.get(player1.getId()).getFirst();
        enchant(forest);

        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(6);

        GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, forest);
        assertThat(bonus.animatedCreature()).isTrue();
        assertThat(bonus.grantedColors()).contains(CardColor.GREEN);
        assertThat(bonus.grantedSubtypes()).contains(CardSubtype.TREEFOLK);
        assertThat(bonus.grantedCardTypes()).contains(CardType.CREATURE);
    }

    @Test
    @DisplayName("Enchanted land is still a land and taps for its normal mana")
    void enchantedLandStillTapsForMana() {
        harness.addToBattlefield(player1, new Forest());
        Permanent forest = gd.playerBattlefields.get(player1.getId()).getFirst();
        enchant(forest);
        // Controlled since last turn: no summoning sickness on the now-creature land.
        forest.setSummoningSick(false);

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        // Still a creature while tapped for mana.
        assertThat(gqs.isCreature(gd, forest)).isTrue();
    }

    @Test
    @DisplayName("Only the enchanted land becomes a creature")
    void onlyEnchantedLandBecomesCreature() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        Permanent enchanted = gd.playerBattlefields.get(player1.getId()).get(0);
        Permanent other = gd.playerBattlefields.get(player1.getId()).get(1);
        enchant(enchanted);

        assertThat(gqs.isCreature(gd, enchanted)).isTrue();
        assertThat(gqs.isCreature(gd, other)).isFalse();
    }

    @Test
    @DisplayName("Land reverts to a non-creature when Living Terrain leaves the battlefield")
    void landRevertsWhenAuraLeaves() {
        harness.addToBattlefield(player1, new Forest());
        Permanent forest = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent aura = enchant(forest);

        assertThat(gqs.isCreature(gd, forest)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(gqs.computeStaticBonus(gd, forest).animatedCreature()).isFalse();
    }

    @Test
    @DisplayName("Cannot cast Living Terrain targeting a non-land permanent")
    void cannotTargetNonLand() {
        harness.addToBattlefield(player1, new Forest()); // valid target so the spell is playable
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        harness.setHand(player1, List.of(new LivingTerrain()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }
}
