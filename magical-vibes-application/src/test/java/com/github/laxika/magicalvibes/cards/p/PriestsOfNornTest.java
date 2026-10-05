package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BrassSquire;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PriestsOfNorn.class, BrassSquire.class})
class PriestsOfNornTest extends BaseCardTest {

    @Test
    void attackingDoesNotTapAndUnblockedDamageGivesPoisonInsteadOfLifeLoss() {
        Permanent priests = addCreatureReady(player1, new PriestsOfNorn());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(priests.isTapped()).isFalse();
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player2, 20);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(priests.isTapped()).isFalse();
    }

    @Test
    void attackingDealsCountersToBlockerInsteadOfMarkedDamage() {
        addCreatureReady(player1, new PriestsOfNorn());
        Permanent blocker = addCreatureReady(player2, new BrassSquire());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Priests of Norn");
        harness.assertOnBattlefield(player2, "Brass Squire");
        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    void blockingDealsCountersToAttackerInsteadOfMarkedDamage() {
        Permanent attacker = addCreatureReady(player1, new BrassSquire());
        addCreatureReady(player2, new PriestsOfNorn());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Brass Squire");
        harness.assertOnBattlefield(player2, "Priests of Norn");
        assertThat(attacker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(attacker.getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    void unblockedDamageAddsToExistingPoisonAndCausesLossAtTen() {
        addCreatureReady(player1, new PriestsOfNorn());
        gd.playerPoisonCounters.put(player2.getId(), 9);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 20);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(10);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }
}
