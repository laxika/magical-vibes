package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FylgjaTest extends BaseCardTest {

    private Permanent enchantBears() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        harness.setHand(player1, List.of(new Fylgja()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        Permanent aura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fylgja"))
                .findFirst().orElseThrow();
        return aura;
    }

    private Permanent enchantedCreature(Permanent aura) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getId().equals(aura.getAttachedTo()))
                .findFirst().orElseThrow();
    }

    private int indexOf(Permanent perm) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(perm);
    }

    @Test
    @DisplayName("Enters with four healing counters")
    void entersWithFourHealingCounters() {
        Permanent aura = enchantBears();
        assertThat(aura.getCounterCount(CounterType.HEALING)).isEqualTo(4);
    }

    @Test
    @DisplayName("Removing a healing counter shields the enchanted creature for 1 damage")
    void removeCounterShieldsEnchantedCreature() {
        Permanent aura = enchantBears();
        Permanent bears = enchantedCreature(aura);

        harness.activateAbility(player1, indexOf(aura), 0, null, null);
        harness.passBothPriorities();

        assertThat(aura.getCounterCount(CounterType.HEALING)).isEqualTo(3);
        assertThat(bears.getDamagePreventionShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Shield prevents the next 1 noncombat damage to the enchanted creature")
    void shieldPreventsNoncombatDamage() {
        Permanent aura = enchantBears();
        Permanent bears = enchantedCreature(aura);
        Permanent pyromancer = new Permanent(new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(pyromancer);

        harness.activateAbility(player1, indexOf(aura), 0, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, indexOf(pyromancer), null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isEqualTo(0);
        assertThat(bears.getDamagePreventionShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot remove a healing counter when none remain")
    void cannotActivateWithoutHealingCounters() {
        Permanent aura = enchantBears();
        aura.setCounterCount(CounterType.HEALING, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(aura), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("{2}{W} puts a healing counter on this Aura")
    void manaAbilityAddsHealingCounter() {
        Permanent aura = enchantBears();
        assertThat(aura.getCounterCount(CounterType.HEALING)).isEqualTo(4);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, indexOf(aura), 1, null, null);
        harness.passBothPriorities();

        assertThat(aura.getCounterCount(CounterType.HEALING)).isEqualTo(5);
    }

    @Test
    @DisplayName("Prevention shield clears at end of turn")
    void shieldClearedAtEndOfTurn() {
        Permanent aura = enchantBears();
        Permanent bears = enchantedCreature(aura);

        harness.activateAbility(player1, indexOf(aura), 0, null, null);
        harness.passBothPriorities();
        assertThat(bears.getDamagePreventionShield()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getDamagePreventionShield()).isEqualTo(0);
    }
}
