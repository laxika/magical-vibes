package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SmolderingButcherTest extends BaseCardTest {

    @Test
    @DisplayName("Wither: combat damage to a blocker is dealt as -1/-1 counters, not marked damage")
    void witherDealsMinusCountersToBlocker() {
        Permanent butcher = addCreatureReady(player1, new SmolderingButcher()); // 4/2, wither
        butcher.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears()); // 2/2
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // All 4 combat damage is dealt to the sole blocker as -1/-1 counters rather than marked damage.
        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(4);
        assertThat(blocker.getMarkedDamage()).isEqualTo(0);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("Wither does not poison players — combat damage to a player is normal life loss")
    void witherDoesNotPoisonPlayer() {
        harness.setLife(player2, 20);

        Permanent butcher = addCreatureReady(player1, new SmolderingButcher()); // 4/2, wither
        butcher.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(0);
    }
}
