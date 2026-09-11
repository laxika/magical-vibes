package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CinderCrawler;
import com.github.laxika.magicalvibes.cards.n.NullBrooch;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpikeCannibal.class, CinderCrawler.class, NullBrooch.class})
class SpikeCannibalTest extends BaseCardTest {

    @Test
    @DisplayName("ETB moves all +1/+1 counters from every creature, including itself")
    void movesAllPlusOnePlusOneCountersFromEveryCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new CinderCrawler());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new CinderCrawler());
        ownCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        opposingCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        castSpikeCannibal();

        Permanent spike = findPermanent(player1, "Spike Cannibal");
        assertThat(spike.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("ETB leaves +1/+1 counters on noncreatures alone")
    void doesNotMoveCountersFromNoncreatures() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new NullBrooch());
        artifact.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);

        castSpikeCannibal();

        Permanent spike = findPermanent(player1, "Spike Cannibal");
        assertThat(spike.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("ETB moves only +1/+1 counters from creatures")
    void doesNotMoveOtherCounterTypes() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CinderCrawler());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ZERO, 4);

        castSpikeCannibal();

        Permanent spike = findPermanent(player1, "Spike Cannibal");
        assertThat(spike.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO)).isEqualTo(4);
    }

    @CardUsed(Solemnity.class)
    @Test
    @DisplayName("ETB does not move counters when its destination cannot receive them")
    void doesNotMoveCountersWhenDestinationCannotReceiveThem() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CinderCrawler());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        castSpikeCannibalSpell();
        harness.passBothPriorities();
        Permanent spike = findPermanent(player1, "Spike Cannibal");
        harness.addToBattlefieldAndReturn(player2, new Solemnity());
        resolveAllTriggers();

        assertThat(spike.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void castSpikeCannibal() {
        castSpikeCannibalSpell();
        resolveAllTriggers();
    }

    private void castSpikeCannibalSpell() {
        harness.setHand(player1, List.of(new SpikeCannibal()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
    }
}
