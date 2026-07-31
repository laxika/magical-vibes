package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LushGrowthTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Lush Growth attaches it to target land")
    void resolvingAttachesToTargetLand() {
        harness.addToBattlefield(player1, new Forest());
        Permanent forest = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new LushGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Lush Growth")
                        && forest.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Enchanted land's subtypes become Mountain, Forest, and Plains")
    void enchantedLandSubtypesOverridden() {
        harness.addToBattlefield(player1, new Island());
        Permanent island = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent aura = new Permanent(new LushGrowth());
        aura.setAttachedTo(island.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, island);

        assertThat(bonus.subtypeOverriding()).isTrue();
        assertThat(bonus.landSubtypeOverriding()).isTrue();
        assertThat(bonus.grantedSubtypes()).containsExactly(
                CardSubtype.MOUNTAIN, CardSubtype.FOREST, CardSubtype.PLAINS);
        assertThat(gqs.effectiveBasicLandTypes(gd, island))
                .containsExactlyInAnyOrder(CardSubtype.MOUNTAIN, CardSubtype.FOREST, CardSubtype.PLAINS);
    }

    @Test
    @DisplayName("Enchanted Island taps for red, green, or white")
    void enchantedLandTapsForRedGreenOrWhite() {
        harness.addToBattlefield(player1, new Island());
        Permanent island = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent aura = new Permanent(new LushGrowth());
        aura.setAttachedTo(island.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getOverriddenLandManaColors(gd, island))
                .containsExactly(ManaColor.RED, ManaColor.GREEN, ManaColor.WHITE);

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Enchanted Swamp can produce white mana")
    void enchantedSwampCanProduceWhite() {
        harness.addToBattlefield(player1, new Swamp());
        Permanent swamp = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent aura = new Permanent(new LushGrowth());
        aura.setAttachedTo(swamp.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        gs.tapPermanent(gd, player1, 0);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(0);
    }

    @Test
    @DisplayName("Normal mana production resumes when Lush Growth leaves")
    void normalManaResumesWhenAuraLeaves() {
        harness.addToBattlefield(player1, new Island());
        Permanent island = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent aura = new Permanent(new LushGrowth());
        aura.setAttachedTo(island.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot cast Lush Growth targeting a non-land permanent")
    void cannotTargetNonLand() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new LushGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }
}
