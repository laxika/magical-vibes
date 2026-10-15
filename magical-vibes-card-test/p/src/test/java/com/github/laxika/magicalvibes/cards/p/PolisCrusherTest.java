package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BowOfNylea;
import com.github.laxika.magicalvibes.cards.l.LeafcrownDryad;
import com.github.laxika.magicalvibes.cards.t.TravelingPhilosopher;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PolisCrusher.class, BowOfNylea.class, TravelingPhilosopher.class, LeafcrownDryad.class})
class PolisCrusherTest extends BaseCardTest {

    @Test
    @DisplayName("Polis Crusher has protection from enchantments")
    void hasProtectionFromEnchantments() {
        Permanent crusher = addReadyCrusher();
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new BowOfNylea());
        Permanent creature = addCreatureReady(player2, new TravelingPhilosopher());

        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, crusher, enchantment)).isTrue();
        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, crusher, creature)).isFalse();
    }

    @Test
    @DisplayName("Polis Crusher becomes monstrous and destroys an enchantment controlled by the damaged player")
    void monstrousCombatDamageDestroysDamagedPlayersEnchantment() {
        Permanent crusher = addReadyCrusher();
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new BowOfNylea());
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(crusher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        crusher.setAttacking(true);
        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(enchantment.getId());

        harness.handlePermanentChosen(player1, enchantment.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Bow of Nylea");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Polis Crusher does not trigger before it becomes monstrous")
    void combatDamageBeforeMonstrousDoesNotDestroyAnEnchantment() {
        Permanent crusher = addReadyCrusher();
        harness.addToBattlefield(player2, new BowOfNylea());
        crusher.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Bow of Nylea");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Monstrosity can be activated again after becoming monstrous but adds no more counters")
    void canActivateMonstrosityAfterBecomingMonstrous() {
        Permanent crusher = addReadyCrusher();
        addMonstrosityMana();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        addMonstrosityMana();
        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(crusher.isMonstrous()).isTrue();
        assertThat(crusher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Two pending monstrosity activations only add counters once")
    void multipleMonstrosityActivationsOnlyAddCountersOnce() {
        Permanent crusher = addReadyCrusher();
        addMonstrosityMana();
        addMonstrosityMana();
        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(crusher.isMonstrous()).isTrue();
        assertThat(crusher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("The combat damage trigger only targets enchantments controlled by the damaged player")
    void combatTriggerExcludesOtherPlayersEnchantmentsAndNonEnchantments() {
        Permanent crusher = addReadyCrusher();
        harness.addToBattlefield(player1, new BowOfNylea());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new BowOfNylea());
        addCreatureReady(player2, new TravelingPhilosopher());
        addMonstrosityMana();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        crusher.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(enchantment.getId());
        harness.handlePermanentChosen(player1, enchantment.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bow of Nylea");
        harness.assertOnBattlefield(player2, "Traveling Philosopher");
        harness.assertNotOnBattlefield(player2, "Bow of Nylea");
    }

    @Test
    @DisplayName("Monstrous combat damage resolves normally when the damaged player has no enchantments")
    void monstrousCombatDamageWithNoLegalTarget() {
        Permanent crusher = addReadyCrusher();
        addMonstrosityMana();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        crusher.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        harness.assertLife(player2, 13);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Protection from enchantments prevents enchantment creatures from blocking")
    void enchantmentCreatureCannotBlock() {
        addReadyCrusher();
        addCreatureReady(player2, new LeafcrownDryad());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Trample deals excess combat damage through a nonenchantment blocker")
    void tramplesOverNonenchantmentCreature() {
        addReadyCrusher();
        Permanent blocker = addCreatureReady(player2, new TravelingPhilosopher());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 2, player2.getId(), 2));

        harness.assertLife(player2, 18);
        harness.assertNotOnBattlefield(player2, "Traveling Philosopher");
        harness.assertOnBattlefield(player1, "Polis Crusher");
    }

    private Permanent addReadyCrusher() {
        return addCreatureReady(player1, new PolisCrusher());
    }

    private void addMonstrosityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
