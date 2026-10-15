package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.e.EsperPanorama;
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
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LushGrowth.class, Forest.class, Island.class, Swamp.class, GrizzlyBears.class, EsperPanorama.class})
class LushGrowthTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Lush Growth attaches it to target land")
    void resolvingAttachesToTargetLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
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
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new LushGrowth());
        aura.setAttachedTo(island.getId());

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
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new LushGrowth());
        aura.setAttachedTo(island.getId());

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
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new LushGrowth());
        aura.setAttachedTo(swamp.getId());

        gs.tapPermanent(gd, player1, 0);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(0);
    }

    @Test
    @DisplayName("Normal mana production resumes when Lush Growth leaves")
    void normalManaResumesWhenAuraLeaves() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new LushGrowth());
        aura.setAttachedTo(island.getId());

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

    @Test
    @DisplayName("An opponent's enchanted land produces red for its controller")
    void opponentLandProducesRedForItsController() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new LushGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, island.getId());
        harness.passBothPriorities();
        harness.tapPermanent(player2, 0);
        harness.handleListChoice(player2, "RED");

        assertThat(island.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Enchanted Panorama loses its printed search ability and colorless production")
    void panoramaLosesPrintedAbilities() {
        Permanent panorama = harness.addToBattlefieldAndReturn(player1, new EsperPanorama());
        harness.setHand(player1, List.of(new LushGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0, panorama.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(panorama);
        assertThat(panorama.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)
                + gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)
                + gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Lush Growth goes to the graveyard if its target leaves before resolution")
    void targetLeavesBeforeResolution() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new LushGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0, island.getId());
        gd.playerBattlefields.get(player2.getId()).remove(island);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Lush Growth");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof LushGrowth);
        assertThat(gd.stack).isEmpty();
    }
}
