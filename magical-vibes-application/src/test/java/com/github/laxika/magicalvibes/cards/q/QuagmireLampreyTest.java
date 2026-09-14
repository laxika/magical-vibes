package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.r.RockBadger;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({QuagmireLamprey.class, RockBadger.class})
class QuagmireLampreyTest extends BaseCardTest {

    @Test
    @DisplayName("When Quagmire Lamprey becomes blocked, the blocker gets a -1/-1 counter")
    void becomesBlockedPutsCounterOnBlocker() {
        Permanent lamprey = addCreatureReady(player1, new QuagmireLamprey());
        lamprey.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new RockBadger());

        declareBlockers(lamprey, blocker);
        harness.passBothPriorities();

        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Quagmire Lamprey puts one counter on each creature blocking it")
    void becomesBlockedByMultipleCreaturesPutsCounterOnEachBlocker() {
        Permanent lamprey = addCreatureReady(player1, new QuagmireLamprey());
        lamprey.setAttacking(true);
        Permanent firstBlocker = addCreatureReady(player2, new RockBadger());
        Permanent secondBlocker = addCreatureReady(player2, new RockBadger());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        resolveAllTriggers();

        assertThat(firstBlocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(secondBlocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Quagmire Lamprey does not put a counter on an unblocked creature")
    void noCounterWhenUnblocked() {
        Permanent lamprey = addCreatureReady(player1, new QuagmireLamprey());
        lamprey.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new RockBadger());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    private void declareBlockers(Permanent lamprey, Permanent blocker) {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(lamprey)
        )));
    }
}
