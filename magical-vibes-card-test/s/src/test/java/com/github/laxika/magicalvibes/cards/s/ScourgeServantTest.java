package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HexplateGolem;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScourgeServant.class, HexplateGolem.class})
class ScourgeServantTest extends BaseCardTest {

    @Test
    void unblockedDamageGivesPoisonInsteadOfLifeLoss() {
        addCreatureReady(player1, new ScourgeServant());
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 20);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(3);
    }

    @Test
    void attackingServantPutsCountersOnBlockerEvenWhenServantDies() {
        addCreatureReady(player1, new ScourgeServant());
        Permanent blocker = addCreatureReady(player2, new HexplateGolem());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Scourge Servant");
        harness.assertNotOnBattlefield(player1, "Scourge Servant");
        harness.assertOnBattlefield(player2, "Hexplate Golem");
        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    void blockingServantPutsCountersOnAttackerEvenWhenServantDies() {
        Permanent attacker = addCreatureReady(player1, new HexplateGolem());
        addCreatureReady(player2, new ScourgeServant());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player2, "Scourge Servant");
        harness.assertNotOnBattlefield(player2, "Scourge Servant");
        harness.assertOnBattlefield(player1, "Hexplate Golem");
        assertThat(attacker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        assertThat(attacker.getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
    }
}
