package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BrassSquire;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhyrexianDigester.class, BrassSquire.class})
class PhyrexianDigesterTest extends BaseCardTest {

    @Test
    void unblockedDamageGivesPoisonInsteadOfLifeLoss() {
        addCreatureReady(player1, new PhyrexianDigester());
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 20);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }

    @Test
    void attackingDigesterPutsCountersOnBlockerEvenWhenItDies() {
        addCreatureReady(player1, new PhyrexianDigester());
        Permanent blocker = addCreatureReady(player2, new BrassSquire());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Phyrexian Digester");
        harness.assertNotOnBattlefield(player1, "Phyrexian Digester");
        harness.assertOnBattlefield(player2, "Brass Squire");
        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    void blockingDigesterPutsCountersOnAttacker() {
        Permanent attacker = addCreatureReady(player1, new BrassSquire());
        addCreatureReady(player2, new PhyrexianDigester());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player2, "Phyrexian Digester");
        harness.assertNotOnBattlefield(player2, "Phyrexian Digester");
        harness.assertOnBattlefield(player1, "Brass Squire");
        assertThat(attacker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }
}
