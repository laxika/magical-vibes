package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DarksteelPlate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RuneOfSpeed.class, GrizzlyBears.class, DarksteelPlate.class})
class RuneOfSpeedTest extends BaseCardTest {

    @Test
    @DisplayName("Rune enters attached to any permanent and draws a card")
    void entersAttachedAndDraws() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new RuneOfSpeed()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Rune of Speed")
                        && bears.getId().equals(p.getAttachedTo()));
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Rune grants +1/+0 and haste to an enchanted creature")
    void enchantedCreatureGetsBoostAndHaste() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent rune = harness.addToBattlefieldAndReturn(player1, new RuneOfSpeed());
        rune.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Rune grants +1/+0 and haste through an enchanted Equipment")
    void enchantedEquipmentBoostsEquippedCreature() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new DarksteelPlate());
        equipment.setAttachedTo(firstCreature.getId());
        Permanent rune = harness.addToBattlefieldAndReturn(player1, new RuneOfSpeed());
        rune.setAttachedTo(equipment.getId());

        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, firstCreature, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondCreature, Keyword.HASTE)).isFalse();

        equipment.setAttachedTo(secondCreature.getId());

        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, firstCreature, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, secondCreature, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Rune does not boost an unattached Equipment")
    void unattachedEquipmentDoesNotReceiveEquipmentBranch() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new DarksteelPlate());
        Permanent rune = harness.addToBattlefieldAndReturn(player1, new RuneOfSpeed());
        rune.setAttachedTo(equipment.getId());

        assertThat(gqs.getEffectivePower(gd, equipment)).isEqualTo(0);
        assertThat(gqs.hasKeyword(gd, equipment, Keyword.HASTE)).isFalse();
    }
    @Test
    @DisplayName("Casting Rune on Equipment draws a card without granting haste to the Equipment")
    void castingOnEquipmentDraws() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new DarksteelPlate());
        harness.setHand(player1, List.of(new RuneOfSpeed()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, equipment.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Rune of Speed")
                        && equipment.getId().equals(p.getAttachedTo()));
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, equipment, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Enchanting an opponent's creature boosts it but draws for the Rune's controller")
    void enchantsOpponentsCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RuneOfSpeed()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Equipment stops granting the Rune's bonuses when the Rune leaves")
    void removingRuneRemovesEquipmentBonuses() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new DarksteelPlate());
        equipment.setAttachedTo(bears.getId());
        Permanent rune = harness.addToBattlefieldAndReturn(player1, new RuneOfSpeed());
        rune.setAttachedTo(equipment.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
        gd.playerBattlefields.get(player1.getId()).remove(rune);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();
    }
}
