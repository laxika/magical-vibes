package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.m.MirranSpy;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Blightwidow.class, MirranSpy.class})
class BlightwidowTest extends BaseCardTest {

    @Test
    void unblockedDamageGivesPoisonInsteadOfLifeLoss() {
        addCreatureReady(player1, new Blightwidow());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 20);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }

    @Test
    void attackingDealsCountersToBlockerInsteadOfMarkedDamage() {
        addCreatureReady(player1, new Blightwidow());
        Permanent spy = addCreatureReady(player2, new MirranSpy());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Blightwidow");
        harness.assertOnBattlefield(player2, "Mirran Spy");
        assertThat(spy.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(spy.getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    void reachAllowsBlockingFlyingAndInfectWorksWhileBlocking() {
        Permanent spy = addCreatureReady(player1, new MirranSpy());
        addCreatureReady(player2, new Blightwidow());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Mirran Spy");
        harness.assertOnBattlefield(player2, "Blightwidow");
        assertThat(spy.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(spy.getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
    }
}
