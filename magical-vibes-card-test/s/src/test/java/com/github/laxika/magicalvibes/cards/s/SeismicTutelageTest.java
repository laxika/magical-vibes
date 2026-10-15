package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EarthbendingStudent;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeismicTutelage.class, EarthbendingStudent.class})
class SeismicTutelageTest extends BaseCardTest {

    @Test
    void entersWithACounterOnTheEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new EarthbendingStudent());

        castSeismicTutelage(creature);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doublesEnchantedCreaturesCountersWhenItAttacks() {
        Permanent creature = addCreatureReady(player1, new EarthbendingStudent());

        castSeismicTutelage(creature);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void doublesAllExistingPlusOneCountersAndLeavesOtherCountersAlone() {
        Permanent creature = addCreatureReady(player1, new EarthbendingStudent());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        creature.setCounterCount(CounterType.CHARGE, 2);

        castSeismicTutelage(creature);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(8);
        assertThat(creature.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    void attackingWithNoPlusOneCountersDoesNotAddAny() {
        Permanent creature = addCreatureReady(player1, new EarthbendingStudent());
        castSeismicTutelage(creature);
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void triggersForAnOpponentsEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new EarthbendingStudent());
        castSeismicTutelage(creature);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void anotherCreatureAttackingDoesNotDoubleEnchantedCreaturesCounters() {
        Permanent creature = addCreatureReady(player1, new EarthbendingStudent());
        addCreatureReady(player1, new EarthbendingStudent());
        castSeismicTutelage(creature);

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void castSeismicTutelage(Permanent creature) {
        harness.setHand(player1, List.of(new SeismicTutelage()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
