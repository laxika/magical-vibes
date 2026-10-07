package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Cagemail;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StrongBack.class, GrizzlyBears.class, LeoninScimitar.class, Cagemail.class})
class StrongBackTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +2/+2 for each attached Aura or Equipment")
    void boostsForEachAttachment() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent strongBack = harness.addToBattlefieldAndReturn(player1, new StrongBack());
        strongBack.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);

        Permanent scimitar = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        scimitar.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(7);
    }

    @Test
    @DisplayName("Equip abilities targeting the enchanted creature cost {3} less")
    void reducesEquipCostForEnchantedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent strongBack = harness.addToBattlefieldAndReturn(player1, new StrongBack());
        strongBack.setAttachedTo(creature.getId());
        Permanent scimitar = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(scimitar),
                null, creature.getId());
        harness.passBothPriorities();

        assertThat(scimitar.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Aura spells targeting the enchanted creature cost {3} less")
    void reducesAuraSpellCostForEnchantedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent strongBack = harness.addToBattlefieldAndReturn(player1, new StrongBack());
        strongBack.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new Cagemail()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("The reductions do not apply when the target is not enchanted by Strong Back")
    void doesNotReduceOtherTargets() {
        Permanent enchantedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent strongBack = harness.addToBattlefieldAndReturn(player1, new StrongBack());
        strongBack.setAttachedTo(enchantedCreature.getId());
        Permanent scimitar = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(scimitar),
                null, otherCreature.getId())).isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new Cagemail()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, otherCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Strong Back resolves attached and counts itself")
    void resolvesAndCountsItself() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new StrongBack()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Strong Back").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Another Strong Back costs only its colored mana and both boosts apply")
    void reducesAnotherStrongBackAndStacksBoosts() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent strongBack = harness.addToBattlefieldAndReturn(player1, new StrongBack());
        strongBack.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new StrongBack()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Strong Back")).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(10);
    }

    @Test
    @DisplayName("Aura reduction cannot pay the colored part of the cost")
    void doesNotReduceColoredMana() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent strongBack = harness.addToBattlefieldAndReturn(player1, new StrongBack());
        strongBack.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new StrongBack()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Attachments controlled by an opponent count and the boost updates when moved")
    void countsOpposingAttachmentsAndUpdatesWhenMoved() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent strongBack = harness.addToBattlefieldAndReturn(player1, new StrongBack());
        strongBack.setAttachedTo(creature.getId());
        Permanent scimitar = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        scimitar.setAttachedTo(creature.getId());
        Permanent cagemail = harness.addToBattlefieldAndReturn(player2, new Cagemail());
        cagemail.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(11);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(11);

        scimitar.setAttachedTo(otherCreature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(8);
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Strong Back does not reduce its opponent's spells or equip abilities")
    void doesNotReduceOpponentsCosts() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent strongBack = harness.addToBattlefieldAndReturn(player1, new StrongBack());
        strongBack.setAttachedTo(creature.getId());
        Permanent scimitar = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(
                player2, gd.playerBattlefields.get(player2.getId()).indexOf(scimitar),
                null, creature.getId())).isInstanceOf(IllegalStateException.class);

        harness.setHand(player2, List.of(new StrongBack()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        assertThatThrownBy(() -> harness.castEnchantment(player2, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
