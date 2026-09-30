package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FencersMagemark;
import com.github.laxika.magicalvibes.cards.g.GruulSignet;
import com.github.laxika.magicalvibes.cards.s.SilhanaLedgewalker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GuardiansMagemark.class, FencersMagemark.class, GruulSignet.class, SilhanaLedgewalker.class})
class GuardiansMagemarkTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Guardian's Magemark attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SilhanaLedgewalker());

        harness.setHand(player1, List.of(new GuardiansMagemark()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof GuardiansMagemark
                        && creature.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Guardian's Magemark boosts enchanted creatures you control")
    void boostsEnchantedCreaturesYouControl() {
        Permanent enchantedCreature = harness.addToBattlefieldAndReturn(player1, new SilhanaLedgewalker());
        Permanent unenchantedCreature = harness.addToBattlefieldAndReturn(player1, new SilhanaLedgewalker());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new SilhanaLedgewalker());

        Permanent magemark = harness.addToBattlefieldAndReturn(player1, new GuardiansMagemark());
        magemark.setAttachedTo(enchantedCreature.getId());

        assertThat(gqs.getEffectivePower(gd, enchantedCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enchantedCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, unenchantedCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, unenchantedCreature)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Guardian's Magemark boosts your creatures enchanted by any Aura")
    void boostsCreaturesEnchantedByAnyAuraYouControl() {
        Permanent otherEnchantedCreature = harness.addToBattlefieldAndReturn(player1, new SilhanaLedgewalker());
        Permanent guardianEnchantedCreature = harness.addToBattlefieldAndReturn(player1, new SilhanaLedgewalker());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new SilhanaLedgewalker());

        Permanent magemark = harness.addToBattlefieldAndReturn(player1, new GuardiansMagemark());
        magemark.setAttachedTo(guardianEnchantedCreature.getId());

        Permanent otherAura = harness.addToBattlefieldAndReturn(player2, new FencersMagemark());
        otherAura.setAttachedTo(otherEnchantedCreature.getId());

        assertThat(gqs.getEffectivePower(gd, otherEnchantedCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherEnchantedCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, guardianEnchantedCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, guardianEnchantedCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Guardian's Magemark stops boosting creatures when it leaves the battlefield")
    void bonusStopsWhenRemoved() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SilhanaLedgewalker());
        Permanent magemark = harness.addToBattlefieldAndReturn(player1, new GuardiansMagemark());
        magemark.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(magemark);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Guardian's Magemark fizzles if its target leaves before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SilhanaLedgewalker());

        harness.setHand(player1, List.of(new GuardiansMagemark()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof GuardiansMagemark);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof GuardiansMagemark);
    }

    @Test
    @DisplayName("Guardian's Magemark cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new GruulSignet());
        harness.setHand(player1, List.of(new GuardiansMagemark()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
