package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KalonianHydra.class, KalonianTusker.class})
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
        Permanent bear = addCreatureReady(player1, new KalonianTusker());
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
        Permanent ownBear = addCreatureReady(player1, new KalonianTusker());
        Permanent enemyBear = harness.addToBattlefieldAndReturn(player2, new KalonianTusker());
        enemyBear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(ownBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(enemyBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Each attacking Hydra doubles counters independently")
    void twoAttackingHydrasDoubleCountersTwice() {
        Permanent first = addCreatureReady(player1, new KalonianHydra());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        Permanent second = addCreatureReady(player1, new KalonianHydra());
        second.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        Permanent tusker = addCreatureReady(player1, new KalonianTusker());
        tusker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(16);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(16);
        assertThat(tusker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Attack trigger uses creatures and counter counts present at resolution")
    void doublesCurrentCountersOnCreaturesAddedAfterAttack() {
        Permanent hydra = addCreatureReady(player1, new KalonianHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);

        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).hasSize(1);
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);
        Permanent tusker = harness.addToBattlefieldAndReturn(player1, new KalonianTusker());
        tusker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        tusker.setCounterCount(CounterType.CHARGE, 3);
        resolveAllTriggers();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(10);
        assertThat(tusker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(tusker.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Attack trigger still doubles other creatures after the Hydra leaves")
    void attackTriggerResolvesWithoutSource() {
        Permanent hydra = addCreatureReady(player1, new KalonianHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        Permanent tusker = addCreatureReady(player1, new KalonianTusker());
        tusker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(hydra);
        gd.playerGraveyards.get(player1.getId()).add(hydra.getCard());
        resolveAllTriggers();

        assertThat(tusker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }
}
