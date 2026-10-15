package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GoldveinPick;
import com.github.laxika.magicalvibes.cards.g.GrizzledOutrider;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RuneOfMortality.class, GrizzledOutrider.class, GoldveinPick.class})
class RuneOfMortalityTest extends BaseCardTest {

    @Test
    @DisplayName("Rune enters attached to any permanent and draws a card")
    void entersAttachedAndDraws() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzledOutrider());

        harness.setHand(player1, List.of(new RuneOfMortality()));
        harness.setLibrary(player1, List.of(new GrizzledOutrider()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Rune of Mortality")
                        && creature.getId().equals(p.getAttachedTo()));
        harness.assertInHand(player1, "Grizzled Outrider");
    }

    @Test
    @DisplayName("Rune grants deathtouch to an enchanted creature")
    void enchantedCreatureHasDeathtouch() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzledOutrider());
        Permanent rune = harness.addToBattlefieldAndReturn(player1, new RuneOfMortality());
        rune.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Rune grants deathtouch through an enchanted Equipment")
    void enchantedEquipmentGrantsDeathtouchToEquippedCreature() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzledOutrider());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzledOutrider());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new GoldveinPick());
        equipment.setAttachedTo(firstCreature.getId());
        Permanent rune = harness.addToBattlefieldAndReturn(player1, new RuneOfMortality());
        rune.setAttachedTo(equipment.getId());

        assertThat(gqs.hasKeyword(gd, firstCreature, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondCreature, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, equipment, Keyword.DEATHTOUCH)).isFalse();

        equipment.setAttachedTo(secondCreature.getId());

        assertThat(gqs.hasKeyword(gd, firstCreature, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, secondCreature, Keyword.DEATHTOUCH)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(rune);

        assertThat(gqs.hasKeyword(gd, secondCreature, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Rune does not grant deathtouch to an unattached Equipment")
    void unattachedEquipmentDoesNotGainDeathtouch() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new GoldveinPick());
        Permanent rune = harness.addToBattlefieldAndReturn(player1, new RuneOfMortality());
        rune.setAttachedTo(equipment.getId());

        assertThat(gqs.hasKeyword(gd, equipment, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Enchanting an opponent creature draws for the Aura controller")
    void enchantingOpponentCreatureDrawsForAuraController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzledOutrider());
        harness.setHand(player1, List.of(new RuneOfMortality()));
        harness.setLibrary(player1, List.of(new GrizzledOutrider()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzled Outrider");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Enchanting noncreature Equipment still draws a card")
    void enchantingEquipmentDrawsWithoutGrantingItDeathtouch() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new GoldveinPick());
        harness.setHand(player1, List.of(new RuneOfMortality()));
        harness.setLibrary(player1, List.of(new GrizzledOutrider()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, equipment.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rune of Mortality");
        harness.assertInHand(player1, "Grizzled Outrider");
        assertThat(gqs.hasKeyword(gd, equipment, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("A Rune with an illegal target does not enter or draw")
    void removedTargetPreventsEnteringAndDrawing() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzledOutrider());
        harness.setHand(player1, List.of(new RuneOfMortality()));
        harness.setLibrary(player1, List.of(new GrizzledOutrider()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerGraveyards.get(player1.getId()).add(creature.getCard());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Rune of Mortality");
        harness.assertInGraveyard(player1, "Rune of Mortality");
        harness.assertNotInHand(player1, "Grizzled Outrider");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
