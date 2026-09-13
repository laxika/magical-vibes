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
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LivingTerrain.class, Forest.class, GrizzlyBears.class})
class LivingTerrainTest extends BaseCardTest {

    private Permanent enchant(Permanent land) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new LivingTerrain());
        aura.setAttachedTo(land.getId());
        return aura;
    }

    @Test
    @DisplayName("Enchanted land is a 5/6 green Treefolk creature")
    void enchantedLandBecomesCreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
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
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        enchant(forest);
        // Controlled since last turn: no summoning sickness on the now-creature land.
        forest.setSummoningSick(false);

        gs.tapPermanent(gd, player1, 0);

        assertThat(gqs.isLand(gd, forest)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        // Still a creature while tapped for mana.
        assertThat(gqs.isCreature(gd, forest)).isTrue();
    }

    @Test
    @DisplayName("Living Terrain can enchant an opponent's land")
    void canEnchantOpponentsLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        enchant(forest);

        assertThat(gqs.isLand(gd, forest)).isTrue();
        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(6);
    }

    @Test
    @DisplayName("Only the enchanted land becomes a creature")
    void onlyEnchantedLandBecomesCreature() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new Forest());
        enchant(enchanted);

        assertThat(gqs.isCreature(gd, enchanted)).isTrue();
        assertThat(gqs.isCreature(gd, other)).isFalse();
    }

    @Test
    @DisplayName("Land reverts to a non-creature when Living Terrain leaves the battlefield")
    void landRevertsWhenAuraLeaves() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = enchant(forest);

        assertThat(gqs.isCreature(gd, forest)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(gqs.computeStaticBonus(gd, forest).animatedCreature()).isFalse();
    }

    @Test
    @DisplayName("Resolving Living Terrain attaches it to the targeted land")
    void resolvingAttachesToTargetedLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new LivingTerrain()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, forest)).isTrue();
        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(6);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Living Terrain")
                        && forest.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Cannot cast Living Terrain targeting a non-land permanent")
    void cannotTargetNonLand() {
        harness.addToBattlefield(player1, new Forest()); // valid target so the spell is playable
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LivingTerrain()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }
}
