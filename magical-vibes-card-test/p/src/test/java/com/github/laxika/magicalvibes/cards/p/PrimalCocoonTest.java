package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({PrimalCocoon.class, RuneclawBear.class})
class PrimalCocoonTest extends BaseCardTest {


    @Test
    @DisplayName("At controller's upkeep, enchanted creature gets a +1/+1 counter")
    void upkeepPutsPlusCounter() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());

        harness.setHand(player1, List.of(new PrimalCocoon()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        int countersBefore = creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(countersBefore + 1);
    }

    @Test
    @DisplayName("Upkeep trigger does not fire during opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());

        harness.setHand(player1, List.of(new PrimalCocoon()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        int countersBefore = creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(countersBefore);
    }

    @Test
    @DisplayName("Counters accumulate over multiple upkeeps")
    void countersAccumulateOverUpkeeps() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());

        harness.setHand(player1, List.of(new PrimalCocoon()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }


    @Test
    @DisplayName("Primal Cocoon is sacrificed when enchanted creature attacks")
    void sacrificedWhenEnchantedCreatureAttacks() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());

        harness.setHand(player1, List.of(new PrimalCocoon()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Primal Cocoon");

        declareAttackers(player1, List.of(0));

        // Resolve sacrifice trigger
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Primal Cocoon");
        harness.assertInGraveyard(player1, "Primal Cocoon");
    }

    @Test
    @DisplayName("Creature keeps +1/+1 counters after Primal Cocoon is sacrificed")
    void creatureKeepsCountersAfterSacrifice() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());

        harness.setHand(player1, List.of(new PrimalCocoon()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        // Accumulate a counter
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        // Attack to trigger sacrifice
        declareAttackers(player1, List.of(0));
        harness.passBothPriorities(); // resolve sacrifice trigger

        // Creature should still have the +1/+1 counter
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        // Cocoon should be gone
        harness.assertNotOnBattlefield(player1, "Primal Cocoon");
    }


    @Test
    @DisplayName("Enchanted blocker sacrifices the Aura controlled by its opponent")
    void sacrificedWhenOpponentsEnchantedCreatureBlocks() {
        addCreatureReady(player1, new RuneclawBear());
        Permanent blocker = addCreatureReady(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new PrimalCocoon()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0, blocker.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        harness.assertOnBattlefield(player1, "Primal Cocoon");
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Primal Cocoon");
        harness.assertInGraveyard(player1, "Primal Cocoon");
    }

    @Test
    @DisplayName("Aura controller's upkeep puts counters on an opposing enchanted creature")
    void upkeepUsesAuraControllerRatherThanCreatureController() {
        Permanent creature = addCreatureReady(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new PrimalCocoon()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opposing enchanted attacker sacrifices the Aura controlled by the defender")
    void sacrificedWhenOpponentsEnchantedCreatureAttacks() {
        Permanent creature = addCreatureReady(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new PrimalCocoon()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Primal Cocoon");
        harness.assertInGraveyard(player1, "Primal Cocoon");
        harness.assertOnBattlefield(player2, "Runeclaw Bear");
    }
}
