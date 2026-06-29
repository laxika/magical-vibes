package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShortSword;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TapUntappedPermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ZahidDjinnOfTheLampTest extends BaseCardTest {

    @Test
    @DisplayName("Has alternate casting cost configured")
    void hasAlternateCastingCost() {
        ZahidDjinnOfTheLamp card = new ZahidDjinnOfTheLamp();

        AlternateHandCast altCast = card.getCastingOption(AlternateHandCast.class).orElseThrow();
        assertThat(altCast.getCost(ManaCastingCost.class).orElseThrow().manaCost()).isEqualTo("{3}{U}");
        assertThat(altCast.getCost(TapUntappedPermanentsCost.class).orElseThrow().count()).isEqualTo(1);
        assertThat(altCast.getCost(TapUntappedPermanentsCost.class).orElseThrow().filter()).isInstanceOf(PermanentIsArtifactPredicate.class);
    }

    @Test
    @DisplayName("Can be cast using alternate cost: pay {3}{U} and tap an untapped artifact")
    void castWithAlternateCost() {
        harness.addToBattlefield(player1, new DarksteelRelic());
        UUID relic = harness.getPermanentId(player1, "Darksteel Relic");

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new ZahidDjinnOfTheLamp()));
        harness.castCreatureWithAlternateCost(player1, 0, List.of(relic));
        harness.passBothPriorities();

        // Zahid should be on battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Zahid, Djinn of the Lamp"));

        // The artifact should be tapped (not sacrificed)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Darksteel Relic") && p.isTapped());

        // Mana should be spent ({3}{U})
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can be cast normally with full mana cost {4}{U}{U}")
    void castWithManaCost() {
        harness.setHand(player1, List.of(new ZahidDjinnOfTheLamp()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Zahid, Djinn of the Lamp"));
    }

    @Test
    @DisplayName("Alternate cost fails if the artifact is already tapped")
    void alternateCostFailsWithTappedArtifact() {
        harness.addToBattlefield(player1, new DarksteelRelic());
        UUID relic = harness.getPermanentId(player1, "Darksteel Relic");

        // Tap the artifact
        gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getId().equals(relic))
                .findFirst().orElseThrow().tap();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new ZahidDjinnOfTheLamp()));

        assertThatThrownBy(() ->
                harness.castCreatureWithAlternateCost(player1, 0, List.of(relic)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Alternate cost fails if target is not an artifact")
    void alternateCostFailsWithNonArtifact() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bears = harness.getPermanentId(player1, "Grizzly Bears");

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new ZahidDjinnOfTheLamp()));

        assertThatThrownBy(() ->
                harness.castCreatureWithAlternateCost(player1, 0, List.of(bears)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does not match");
    }

    @Test
    @DisplayName("Alternate cost fails with insufficient mana")
    void alternateCostFailsWithInsufficientMana() {
        harness.addToBattlefield(player1, new DarksteelRelic());
        UUID relic = harness.getPermanentId(player1, "Darksteel Relic");

        // Only 2 mana instead of {3}{U}
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new ZahidDjinnOfTheLamp()));

        assertThatThrownBy(() ->
                harness.castCreatureWithAlternateCost(player1, 0, List.of(relic)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Alternate cost does not sacrifice the artifact")
    void alternateCostDoesNotSacrificeArtifact() {
        harness.addToBattlefield(player1, new DarksteelRelic());
        UUID relic = harness.getPermanentId(player1, "Darksteel Relic");

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new ZahidDjinnOfTheLamp()));
        harness.castCreatureWithAlternateCost(player1, 0, List.of(relic));
        harness.passBothPriorities();

        // Artifact should still be on battlefield (tapped, not sacrificed)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Darksteel Relic"));

        // Graveyard should NOT contain the artifact
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Darksteel Relic"));
    }

    @Test
    @DisplayName("Alternate cost works with any artifact type (equipment)")
    void alternateCostWorksWithEquipment() {
        harness.addToBattlefield(player1, new ShortSword());
        UUID sword = harness.getPermanentId(player1, "Short Sword");

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new ZahidDjinnOfTheLamp()));
        harness.castCreatureWithAlternateCost(player1, 0, List.of(sword));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Zahid, Djinn of the Lamp"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Short Sword") && p.isTapped());
    }

    @Test
    @DisplayName("Alternate cost fails if no permanent ID is provided")
    void alternateCostFailsWithNoPermanent() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new ZahidDjinnOfTheLamp()));

        assertThatThrownBy(() ->
                harness.castCreatureWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }
}
