package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Fylgja.class, BalduvianBears.class, ZuranSpellcaster.class})
class FylgjaTest extends BaseCardTest {

    private Permanent enchantBears() {
        Permanent bears = addCreatureReady(player1, new BalduvianBears());
        return enchantWithFylgja(bears);
    }

    private Permanent enchantWithFylgja(Permanent creature) {
        harness.setHand(player1, List.of(new Fylgja()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        Permanent aura = findPermanent(player1, "Fylgja");
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
        Permanent spellcaster = addCreatureReady(player1, new ZuranSpellcaster());

        harness.activateAbility(player1, indexOf(aura), 0, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, indexOf(spellcaster), null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isEqualTo(0);
        assertThat(bears.getDamagePreventionShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Shield prevents the next 1 combat damage to the enchanted creature")
    void shieldPreventsCombatDamage() {
        Permanent aura = enchantBears();
        Permanent bears = enchantedCreature(aura);
        addCreatureReady(player2, new BalduvianBears());

        harness.activateAbility(player1, indexOf(aura), 0, null, null);
        harness.passBothPriorities();

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(indexOf(bears), 0)));
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isEqualTo(1);
        assertThat(bears.getDamagePreventionShield()).isEqualTo(0);
    }

    @Test
    void canEnchantAndProtectOpponentsCreature() {
        Permanent opponentBears = addCreatureReady(player2, new BalduvianBears());
        Permanent opponentSpellcaster = addCreatureReady(player2, new ZuranSpellcaster());
        Permanent aura = enchantWithFylgja(opponentBears);

        harness.activateAbility(player1, indexOf(aura), 0, null, null);
        harness.passBothPriorities();

        int opponentSpellcasterIndex = gd.playerBattlefields.get(player2.getId()).indexOf(opponentSpellcaster);
        harness.activateAbility(player2, opponentSpellcasterIndex, null, opponentBears.getId());
        harness.passBothPriorities();

        assertThat(opponentBears.getMarkedDamage()).isZero();
        assertThat(opponentBears.getDamagePreventionShield()).isZero();
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
