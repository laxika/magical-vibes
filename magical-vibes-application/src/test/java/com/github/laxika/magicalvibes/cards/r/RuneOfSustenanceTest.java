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

class RuneOfSustenanceTest extends BaseCardTest {

    @Test
    @DisplayName("Rune enters attached to any permanent and draws a card")
    void entersAttachedAndDraws() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.setHand(player1, List.of(new RuneOfSustenance()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Rune of Sustenance")
                        && bears.getId().equals(p.getAttachedTo()));
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Rune grants lifelink to an enchanted creature")
    void enchantedCreatureHasLifelink() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent rune = new Permanent(new RuneOfSustenance());
        rune.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(rune);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Rune grants lifelink through an enchanted Equipment")
    void enchantedEquipmentGrantsLifelinkToEquippedCreature() {
        Permanent firstCreature = new Permanent(new GrizzlyBears());
        Permanent secondCreature = new Permanent(new GrizzlyBears());
        Permanent equipment = new Permanent(new DarksteelPlate());
        equipment.setAttachedTo(firstCreature.getId());
        Permanent rune = new Permanent(new RuneOfSustenance());
        rune.setAttachedTo(equipment.getId());

        gd.playerBattlefields.get(player1.getId()).add(firstCreature);
        gd.playerBattlefields.get(player1.getId()).add(secondCreature);
        gd.playerBattlefields.get(player1.getId()).add(equipment);
        gd.playerBattlefields.get(player1.getId()).add(rune);

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
        Permanent equipment = new Permanent(new DarksteelPlate());
        Permanent rune = new Permanent(new RuneOfSustenance());
        rune.setAttachedTo(equipment.getId());

        gd.playerBattlefields.get(player1.getId()).add(equipment);
        gd.playerBattlefields.get(player1.getId()).add(rune);

        assertThat(gqs.hasKeyword(gd, equipment, Keyword.LIFELINK)).isFalse();
    }
}
