package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BorealDruid;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShapeOfTheWiitigo.class, BorealDruid.class})
class ShapeOfTheWiitigoTest extends BaseCardTest {

    @Test
    @DisplayName("When Shape of the Wiitigo enters, enchanted creature gets six +1/+1 counters")
    void entersWithSixCountersOnEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new BorealDruid());

        castShape(creature);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    @DisplayName("Upkeep removes a +1/+1 counter when enchanted creature did not attack or block")
    void upkeepRemovesCounterWithoutCombat() {
        Permanent creature = addCreatureReady(player1, new BorealDruid());
        castShape(creature);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("The upkeep ability does not trigger during an opponent's upkeep")
    void upkeepDoesNotTriggerDuringOpponentsUpkeep() {
        Permanent creature = addCreatureReady(player1, new BorealDruid());
        castShape(creature);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    @DisplayName("Upkeep adds a +1/+1 counter after enchanted creature attacked")
    void upkeepAddsCounterAfterAttack() {
        Permanent creature = addCreatureReady(player1, new BorealDruid());
        castShape(creature);

        declareAttackers(List.of(0));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
    }

    @Test
    @DisplayName("Upkeep adds a +1/+1 counter after enchanted creature blocked")
    void upkeepAddsCounterAfterBlock() {
        Permanent creature = addCreatureReady(player1, new BorealDruid());
        castShape(creature);
        addCreatureReady(player2, new BorealDruid());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
    }

    @Test
    @DisplayName("The combat window is consumed after Shape of the Wiitigo's upkeep trigger")
    void combatWindowIsConsumed() {
        Permanent creature = addCreatureReady(player1, new BorealDruid());
        castShape(creature);

        declareAttackers(List.of(0));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    private void castShape(Permanent creature) {
        harness.setHand(player1, List.of(new ShapeOfTheWiitigo()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
