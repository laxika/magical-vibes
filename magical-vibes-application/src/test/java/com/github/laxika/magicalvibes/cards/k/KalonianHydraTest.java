package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KalonianHydraTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with four +1/+1 counters")
    void entersWithFourCounters() {
        harness.setHand(player1, List.of(new KalonianHydra()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent hydra = gd.playerBattlefields.get(player1.getId()).get(0);
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Attacking doubles +1/+1 counters on each creature you control")
    void attackDoublesCountersOnControlledCreatures() {
        Permanent hydra = addCreatureReady(player1, new KalonianHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        bear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(8);
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    @DisplayName("Creatures without +1/+1 counters and opponent creatures are unaffected")
    void leavesCounterlessAndOpposingCreaturesAlone() {
        Permanent hydra = addCreatureReady(player1, new KalonianHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent enemyBear = gd.playerBattlefields.get(player2.getId()).get(0);
        enemyBear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(ownBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(enemyBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }
}
