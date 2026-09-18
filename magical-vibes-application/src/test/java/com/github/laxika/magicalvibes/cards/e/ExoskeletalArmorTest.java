package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.k.KrosanVerge;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExoskeletalArmor.class, KrosanVerge.class, SuntailHawk.class})
class ExoskeletalArmorTest extends BaseCardTest {

    @Test
    void boostsEnchantedCreatureByCreatureCardsInAllGraveyards() {
        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        harness.setGraveyard(player1, List.of(new SuntailHawk(), new KrosanVerge()));
        harness.setGraveyard(player2, List.of(new SuntailHawk()));

        harness.setHand(player1, List.of(new ExoskeletalArmor()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, hawk.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hawk)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hawk)).isEqualTo(3);

        gd.playerGraveyards.get(player2.getId()).add(new SuntailHawk());
        assertThat(gqs.getEffectivePower(gd, hawk)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, hawk)).isEqualTo(4);
    }

    @Test
    void stopsBoostingWhenAuraLeavesTheBattlefield() {
        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new ExoskeletalArmor());
        armor.setAttachedTo(hawk.getId());
        harness.setGraveyard(player1, List.of(new SuntailHawk()));

        assertThat(gqs.getEffectivePower(gd, hawk)).isEqualTo(2);
        gd.playerBattlefields.get(player1.getId()).remove(armor);
        assertThat(gqs.getEffectivePower(gd, hawk)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, hawk)).isEqualTo(1);
    }

    @Test
    void cannotEnchantNonCreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new KrosanVerge());
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
