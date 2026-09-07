package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BoneSaw;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pariah;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WithGreatPower.class, GrizzlyBears.class, Pariah.class, BoneSaw.class, Shock.class})
class WithGreatPowerTest extends BaseCardTest {

    @Test
    void enchantedCreatureGetsBonusForEachAuraAndEquipmentAttached() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = castWithGreatPower(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);

        castPariah(creature);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);

        castAndEquipBoneSaw(creature);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(8);
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void damageToControllerIsRedirectedToEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castWithGreatPower(creature);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }

    @Test
    void canEnchantOnlyCreatureYouControl() {
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WithGreatPower()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    private Permanent castWithGreatPower(Permanent creature) {
        harness.setHand(player1, List.of(new WithGreatPower()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        return findPermanent(player1, "With Great Power . . .");
    }

    private void castPariah(Permanent creature) {
        harness.setHand(player1, List.of(new Pariah()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
    }

    private void castAndEquipBoneSaw(Permanent creature) {
        harness.setHand(player1, List.of(new BoneSaw()));
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent boneSaw = findPermanent(player1, "Bone Saw");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(boneSaw), null, creature.getId());
        harness.passBothPriorities();
    }
}
