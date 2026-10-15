package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DepartTheRealm;
import com.github.laxika.magicalvibes.cards.f.FacelessHaven;
import com.github.laxika.magicalvibes.cards.f.FearlessPup;
import com.github.laxika.magicalvibes.cards.g.GoldveinPick;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RuneOfFlight.class, FearlessPup.class, GoldveinPick.class, DepartTheRealm.class, FacelessHaven.class})
class RuneOfFlightTest extends BaseCardTest {

    @Test
    @DisplayName("Rune enters attached to any permanent and draws a card")
    void entersAttachedAndDraws() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FearlessPup());

        harness.setHand(player1, List.of(new RuneOfFlight()));
        harness.setLibrary(player1, List.of(new FearlessPup()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Rune of Flight")
                        && creature.getId().equals(p.getAttachedTo()));
        harness.assertInHand(player1, "Fearless Pup");
    }

    @Test
    @DisplayName("Rune grants flying to an enchanted creature")
    void enchantedCreatureHasFlying() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FearlessPup());

        Permanent rune = harness.addToBattlefieldAndReturn(player1, new RuneOfFlight());
        rune.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Rune grants flying through an enchanted Equipment")
    void enchantedEquipmentGrantsFlyingToEquippedCreature() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new FearlessPup());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new FearlessPup());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new GoldveinPick());
        equipment.setAttachedTo(firstCreature.getId());
        Permanent rune = harness.addToBattlefieldAndReturn(player1, new RuneOfFlight());
        rune.setAttachedTo(equipment.getId());

        assertThat(gqs.hasKeyword(gd, firstCreature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondCreature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, equipment, Keyword.FLYING)).isFalse();

        equipment.setAttachedTo(secondCreature.getId());

        assertThat(gqs.hasKeyword(gd, firstCreature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, secondCreature, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(rune);

        assertThat(gqs.hasKeyword(gd, secondCreature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Rune does not grant flying to an unattached Equipment")
    void unattachedEquipmentDoesNotGainFlying() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new GoldveinPick());
        Permanent rune = harness.addToBattlefieldAndReturn(player1, new RuneOfFlight());
        rune.setAttachedTo(equipment.getId());

        assertThat(gqs.hasKeyword(gd, equipment, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Enchanting an opponent's Equipment draws for the Rune's controller")
    void enchantsOpponentsEquipmentAndDrawsForAuraController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new FearlessPup());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new GoldveinPick());
        equipment.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new RuneOfFlight()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new FearlessPup()));
        harness.setLibrary(player2, List.of(new GoldveinPick()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, equipment.getId());
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Fearless Pup");
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, equipment, Keyword.FLYING)).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Rune of Flight")
                        && equipment.getId().equals(p.getAttachedTo()));

        harness.passBothPriorities();

        harness.assertInHand(player1, "Fearless Pup");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Rune can enchant a land without granting it flying")
    void enchantsLandAndDrawsWithoutGrantingFlying() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new FacelessHaven());
        harness.setHand(player1, List.of(new RuneOfFlight()));
        harness.setLibrary(player1, List.of(new FearlessPup()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, land.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Rune of Flight")
                        && land.getId().equals(p.getAttachedTo()));
        assertThat(gqs.hasKeyword(gd, land, Keyword.FLYING)).isFalse();
        harness.assertInHand(player1, "Fearless Pup");
    }

    @Test
    @DisplayName("An illegal target prevents the Rune from entering and drawing")
    void targetReturnedBeforeResolutionPreventsDraw() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new FearlessPup());
        harness.setHand(player1, List.of(new RuneOfFlight()));
        harness.setHand(player2, List.of(new DepartTheRealm()));
        harness.setLibrary(player1, List.of(new GoldveinPick()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Fearless Pup");
        harness.assertInGraveyard(player1, "Rune of Flight");
        harness.assertNotOnBattlefield(player1, "Rune of Flight");
        harness.assertNotInHand(player1, "Goldvein Pick");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
