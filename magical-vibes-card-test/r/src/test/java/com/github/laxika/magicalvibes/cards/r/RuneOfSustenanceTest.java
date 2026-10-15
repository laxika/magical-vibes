package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BeskirShieldmate;
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

@CardUsed({RuneOfSustenance.class, BeskirShieldmate.class, GoldveinPick.class})
class RuneOfSustenanceTest extends BaseCardTest {

    @Test
    @DisplayName("Rune enters attached to any permanent and draws a card")
    void entersAttachedAndDraws() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BeskirShieldmate());

        harness.setHand(player1, List.of(new RuneOfSustenance()));
        harness.setLibrary(player1, List.of(new BeskirShieldmate()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Rune of Sustenance")
                        && creature.getId().equals(p.getAttachedTo()));
        harness.assertInHand(player1, "Beskir Shieldmate");
    }

    @Test
    @DisplayName("Rune grants lifelink to an enchanted creature")
    void enchantedCreatureHasLifelink() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BeskirShieldmate());

        Permanent rune = harness.addToBattlefieldAndReturn(player1, new RuneOfSustenance());
        rune.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Rune grants lifelink through an enchanted Equipment")
    void enchantedEquipmentGrantsLifelinkToEquippedCreature() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new BeskirShieldmate());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new BeskirShieldmate());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new GoldveinPick());
        equipment.setAttachedTo(firstCreature.getId());
        Permanent rune = harness.addToBattlefieldAndReturn(player1, new RuneOfSustenance());
        rune.setAttachedTo(equipment.getId());

        assertThat(gqs.hasKeyword(gd, firstCreature, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondCreature, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, equipment, Keyword.LIFELINK)).isFalse();

        equipment.setAttachedTo(secondCreature.getId());

        assertThat(gqs.hasKeyword(gd, firstCreature, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, secondCreature, Keyword.LIFELINK)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(rune);

        assertThat(gqs.hasKeyword(gd, secondCreature, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Rune does not grant lifelink to an unattached Equipment")
    void unattachedEquipmentDoesNotGainLifelink() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new GoldveinPick());
        Permanent rune = harness.addToBattlefieldAndReturn(player1, new RuneOfSustenance());
        rune.setAttachedTo(equipment.getId());

        assertThat(gqs.hasKeyword(gd, equipment, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Enchanting an opposing creature draws for the Rune controller")
    void enchantingOpposingCreatureDrawsForAuraController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BeskirShieldmate());
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new RuneOfSustenance()));
        harness.setLibrary(player1, List.of(new BeskirShieldmate()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Beskir Shieldmate");
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Enchanting Equipment draws without granting lifelink to the Equipment")
    void castingOnEquipmentDraws() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new GoldveinPick());
        harness.setHand(player1, List.of(new RuneOfSustenance()));
        harness.setLibrary(player1, List.of(new BeskirShieldmate()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, equipment.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Beskir Shieldmate");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof RuneOfSustenance
                        && equipment.getId().equals(p.getAttachedTo()));
        assertThat(gqs.hasKeyword(gd, equipment, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Rune can enchant a noncreature, non-Equipment permanent")
    void enchantsAnotherAuraWithoutGrantingLifelink() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BeskirShieldmate());
        Permanent firstRune = harness.addToBattlefieldAndReturn(player1, new RuneOfSustenance());
        firstRune.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new RuneOfSustenance()));
        harness.setLibrary(player1, List.of(new BeskirShieldmate()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, firstRune.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Beskir Shieldmate");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof RuneOfSustenance
                        && firstRune.getId().equals(p.getAttachedTo()));
        assertThat(gqs.hasKeyword(gd, firstRune, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Lifelink gains life for the creature controller rather than the Rune controller")
    void opposingCreatureControllerGainsLifeFromCombat() {
        Permanent creature = addCreatureReady(player2, new BeskirShieldmate());
        Permanent rune = harness.addToBattlefieldAndReturn(player1, new RuneOfSustenance());
        rune.setAttachedTo(creature.getId());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 22);
    }
}
