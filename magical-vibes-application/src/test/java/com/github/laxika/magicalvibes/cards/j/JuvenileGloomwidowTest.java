package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JuvenileGloomwidowTest extends BaseCardTest {

    // ===== Behavior: native wither deals combat damage as -1/-1 counters =====

    @Test
    @DisplayName("Deals combat damage to a blocker as -1/-1 counters, not marked damage")
    void witherDealsMinusCountersToBlocker() {
        Permanent widow = addReady(player1, new JuvenileGloomwidow()); // 1/3, wither
        widow.setAttacking(true);

        Permanent blocker = addReady(player2, new GrizzlyBears()); // 2/2
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // 1 power dealt as a -1/-1 counter rather than marked damage; blocker survives as a 1/1.
        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(blocker.getMarkedDamage()).isEqualTo(0);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
    }

    @Test
    @DisplayName("Wither does not poison a player — combat damage is normal life loss")
    void witherDoesNotPoisonPlayer() {
        harness.setLife(player2, 20);

        Permanent widow = addReady(player1, new JuvenileGloomwidow()); // 1/3, wither
        widow.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(0);
    }

    // ===== Helpers =====

    private Permanent addReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
