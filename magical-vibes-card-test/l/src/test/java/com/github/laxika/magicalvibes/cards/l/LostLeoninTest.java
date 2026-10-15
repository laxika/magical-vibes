package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.p.PhyrexianHulk;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LostLeonin.class, PhyrexianHulk.class})
class LostLeoninTest extends BaseCardTest {

    @Test
    void unblockedDamageGivesPoisonInsteadOfLifeLoss() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new LostLeonin());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 20);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    void attackingDamagePutsCountersOnBlockerWithoutMarkedDamage() {
        addCreatureReady(player1, new LostLeonin());
        Permanent blocker = addCreatureReady(player2, new PhyrexianHulk());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Lost Leonin");
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    void blockingDamagePutsCountersOnAttackerWithoutMarkedDamage() {
        Permanent attacker = addCreatureReady(player1, new PhyrexianHulk());
        addCreatureReady(player2, new LostLeonin());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(attacker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(attacker.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Lost Leonin");
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    void infectCountersKillCreatureWithOneToughness() {
        addCreatureReady(player1, new LostLeonin());
        addCreatureReady(player2, new LostLeonin());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertNotOnBattlefield(player1, "Lost Leonin");
        harness.assertNotOnBattlefield(player2, "Lost Leonin");
        harness.assertInGraveyard(player1, "Lost Leonin");
        harness.assertInGraveyard(player2, "Lost Leonin");
    }

    @Test
    void tenthPoisonCounterLosesGameEvenAtPositiveLife() {
        harness.setLife(player2, 20);
        gd.playerPoisonCounters.put(player2.getId(), 8);
        addCreatureReady(player1, new LostLeonin());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 20);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(10);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }
}
