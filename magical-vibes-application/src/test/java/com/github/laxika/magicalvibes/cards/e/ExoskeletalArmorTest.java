package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DwarvenDriller;
import com.github.laxika.magicalvibes.cards.m.MentalNote;
import com.github.laxika.magicalvibes.cards.r.RiftstonePortal;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DwarvenDriller.class, ExoskeletalArmor.class, MentalNote.class, RiftstonePortal.class, SuntailHawk.class})
class ExoskeletalArmorTest extends BaseCardTest {

    @Test
    void boostsEnchantedCreatureByCreatureCardsInAllGraveyards() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DwarvenDriller());
        harness.setGraveyard(player1, List.of(new DwarvenDriller(), new MentalNote()));
        harness.setGraveyard(player2, List.of(new DwarvenDriller()));

        harness.setHand(player1, List.of(new ExoskeletalArmor()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);

        harness.setGraveyard(player2, List.of(new DwarvenDriller(), new DwarvenDriller()));
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    void stopsBoostingWhenAuraLeavesTheBattlefield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DwarvenDriller());
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new ExoskeletalArmor());
        armor.setAttachedTo(creature.getId());
        harness.setGraveyard(player1, List.of(new DwarvenDriller()));

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        gd.playerBattlefields.get(player1.getId()).remove(armor);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void canEnchantCreatureControlledByOpponent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DwarvenDriller());
        harness.setGraveyard(player1, List.of(new MentalNote()));
        harness.setGraveyard(player2, List.of(new DwarvenDriller()));

        harness.setHand(player1, List.of(new ExoskeletalArmor()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    void cannotEnchantNonCreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new RiftstonePortal());
        harness.setHand(player1, List.of(new ExoskeletalArmor()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void fizzlesWhenEnchantedCreatureLeavesBeforeResolution() {
        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        ExoskeletalArmor armor = new ExoskeletalArmor();
        harness.setHand(player1, List.of(armor));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, hawk.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, hawk));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Exoskeletal Armor");
        harness.assertNotOnBattlefield(player1, "Exoskeletal Armor");
    }
}
