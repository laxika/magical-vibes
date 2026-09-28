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
}
