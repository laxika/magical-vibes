package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BorealDruid;
import com.github.laxika.magicalvibes.cards.r.RonomUnicorn;
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

@CardUsed({ShapeOfTheWiitigo.class, BorealDruid.class, RonomUnicorn.class})
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

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

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

    @Test
    @DisplayName("Each Shape adds a counter when their shared enchanted creature attacked")
    void multipleShapesEachAddCounterAfterAttack() {
        Permanent creature = addCreatureReady(player1, new BorealDruid());
        castShape(creature);
        castShape(creature);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(12);

        declareAttackers(List.of(0));
        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(14);
    }

    @Test
    @DisplayName("Combat before the previous upkeep does not count for a newly cast Shape")
    void ignoresCombatBeforePreviousUpkeep() {
        Permanent creature = addCreatureReady(player1, new BorealDruid());
        declareAttackers(List.of(0));
        advanceToUpkeep(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        castShape(creature);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Upkeep still removes a counter if the Aura is destroyed in response")
    void upkeepResolvesAfterAuraIsDestroyed() {
        Permanent creature = addCreatureReady(player1, new BorealDruid());
        castShape(creature);
        Permanent aura = findPermanent(player1, "Shape of the Wiitigo");
        harness.addToBattlefield(player1, new RonomUnicorn());

        harness.withAutoStop(TurnStep.UPKEEP, () -> advanceToUpkeep(player1));
        assertThat(gd.stack).hasSize(1);
        harness.activateAbility(player1, 2, null, aura.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Shape of the Wiitigo");
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Upkeep follows the Aura controller when it enchants an opposing creature")
    void auraControllersUpkeepAdjustsOpposingCreature() {
        Permanent creature = addCreatureReady(player2, new BorealDruid());
        castShape(creature);

        advanceToUpkeep(player2);
        resolveAllTriggers();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);

        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Removing a counter from a creature with no counters does nothing")
    void upkeepWithoutCountersDoesNothing() {
        Permanent creature = addCreatureReady(player1, new BorealDruid());
        castShape(creature);
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertOnBattlefield(player1, "Boreal Druid");
    }

    private void castShape(Permanent creature) {
        harness.setHand(player1, List.of(new ShapeOfTheWiitigo()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}
