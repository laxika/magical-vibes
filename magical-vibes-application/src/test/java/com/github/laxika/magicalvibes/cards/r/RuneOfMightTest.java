package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DarksteelPlate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RuneOfMightTest extends BaseCardTest {

    @Test
    @DisplayName("Rune enters attached to any permanent and draws a card")
    void entersAttachedAndDraws() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.setHand(player1, List.of(new RuneOfMight()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Rune of Might")
                        && bears.getId().equals(p.getAttachedTo()));
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Rune grants +1/+1 and trample to an enchanted creature")
    void enchantedCreatureGetsBoostAndTrample() {
        Permanent bears = new Permanent(new GrizzlyBears());
        Permanent rune = new Permanent(new RuneOfMight());
        rune.setAttachedTo(bears.getId());

        gd.playerBattlefields.get(player1.getId()).add(bears);
        gd.playerBattlefields.get(player1.getId()).add(rune);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Rune grants +1/+1 and trample through an enchanted Equipment")
    void enchantedEquipmentBoostsEquippedCreature() {
        Permanent firstCreature = new Permanent(new GrizzlyBears());
        Permanent secondCreature = new Permanent(new GrizzlyBears());
        Permanent equipment = new Permanent(new DarksteelPlate());
        equipment.setAttachedTo(firstCreature.getId());
        Permanent rune = new Permanent(new RuneOfMight());
        rune.setAttachedTo(equipment.getId());

        gd.playerBattlefields.get(player1.getId()).add(firstCreature);
        gd.playerBattlefields.get(player1.getId()).add(secondCreature);
        gd.playerBattlefields.get(player1.getId()).add(equipment);
        gd.playerBattlefields.get(player1.getId()).add(rune);

        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, firstCreature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, firstCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondCreature, Keyword.TRAMPLE)).isFalse();

        equipment.setAttachedTo(secondCreature.getId());

        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, firstCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, secondCreature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, firstCreature, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, secondCreature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Rune does not boost an unattached Equipment")
    void unattachedEquipmentDoesNotReceiveEquipmentBranch() {
        Permanent equipment = new Permanent(new DarksteelPlate());
        Permanent rune = new Permanent(new RuneOfMight());
        rune.setAttachedTo(equipment.getId());

        gd.playerBattlefields.get(player1.getId()).add(equipment);
        gd.playerBattlefields.get(player1.getId()).add(rune);

        assertThat(gqs.getEffectivePower(gd, equipment)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, equipment)).isEqualTo(0);
        assertThat(gqs.hasKeyword(gd, equipment, Keyword.TRAMPLE)).isFalse();
    }
}
