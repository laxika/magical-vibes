package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhantasmalTerrainTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Phantasmal Terrain attaches it and awaits basic land type choice")
    void resolvingTriggersBasicLandTypeChoice() {
        harness.addToBattlefield(player1, new Forest());
        Permanent forest = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new PhantasmalTerrain()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Phantasmal Terrain")
                        && forest.getId().equals(p.getAttachedTo()));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    @Test
    @DisplayName("Choosing a basic land type sets chosenSubtype on the permanent")
    void choosingTypeSetsOnPermanent() {
        harness.addToBattlefield(player1, new Forest());
        Permanent forest = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new PhantasmalTerrain()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ISLAND");

        Permanent terrain = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phantasmal Terrain"))
                .findFirst().orElseThrow();
        assertThat(terrain.getChosenSubtype()).isEqualTo(CardSubtype.ISLAND);
    }

    @Test
    @DisplayName("Enchanted Mountain produces black mana when Swamp is chosen")
    void enchantedMountainProducesBlackWhenSwampChosen() {
        harness.addToBattlefield(player1, new Mountain());
        Permanent mountain = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent aura = new Permanent(new PhantasmalTerrain());
        aura.setAttachedTo(mountain.getId());
        aura.setChosenSubtype(CardSubtype.SWAMP);
        gd.playerBattlefields.get(player1.getId()).add(aura);

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(0);
    }

    @Test
    @DisplayName("Enchanted land's subtypes are overridden to chosen type only")
    void enchantedLandSubtypesOverriddenToChosenType() {
        harness.addToBattlefield(player1, new Forest());
        Permanent forest = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent aura = new Permanent(new PhantasmalTerrain());
        aura.setAttachedTo(forest.getId());
        aura.setChosenSubtype(CardSubtype.PLAINS);
        gd.playerBattlefields.get(player1.getId()).add(aura);

        GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, forest);

        assertThat(bonus.subtypeOverriding()).isTrue();
        assertThat(bonus.landSubtypeOverriding()).isTrue();
        assertThat(bonus.grantedSubtypes()).containsExactly(CardSubtype.PLAINS);
    }

    @Test
    @DisplayName("Normal mana production resumes when Phantasmal Terrain leaves battlefield")
    void normalManaResumesWhenAuraLeaves() {
        harness.addToBattlefield(player1, new Forest());
        Permanent forest = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent aura = new Permanent(new PhantasmalTerrain());
        aura.setAttachedTo(forest.getId());
        aura.setChosenSubtype(CardSubtype.ISLAND);
        gd.playerBattlefields.get(player1.getId()).add(aura);

        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot cast Phantasmal Terrain targeting a non-land permanent")
    void cannotTargetNonLand() {
        harness.addToBattlefield(player1, new Forest()); // valid target so spell is playable
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        harness.setHand(player1, List.of(new PhantasmalTerrain()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }
}
