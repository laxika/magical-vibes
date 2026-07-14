package com.github.laxika.magicalvibes.cards.s;

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

class SmolderingButcherTest extends BaseCardTest {

    @Test
    @DisplayName("Wither: combat damage to a blocker is dealt as -1/-1 counters, not marked damage")
    void witherDealsMinusCountersToBlocker() {
        Permanent butcher = addReady(player1, new SmolderingButcher()); // 4/2, wither
        butcher.setAttacking(true);

        Permanent blocker = addReady(player2, new GrizzlyBears()); // 2/2
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // Lethal (the blocker's 2 toughness) is dealt as -1/-1 counters rather than marked damage;
        // the 2/2 becomes 0/0 and dies.
        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(blocker.getMarkedDamage()).isEqualTo(0);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("Wither does not poison players — combat damage to a player is normal life loss")
    void witherDoesNotPoisonPlayer() {
        harness.setLife(player2, 20);

        Permanent butcher = addReady(player1, new SmolderingButcher()); // 4/2, wither
        butcher.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(0);
    }

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
