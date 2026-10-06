package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BriarberryCohort;
import com.github.laxika.magicalvibes.cards.c.CrabappleCohort;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RustrazorButcher.class, CrabappleCohort.class, BriarberryCohort.class})
class RustrazorButcherTest extends BaseCardTest {

    @Test
    @DisplayName("Wither deals first-strike combat damage to a blocker as -1/-1 counters")
    void witherDealsMinusCounterToBlocker() {
        // 4/4 blocker survives the single point of wither damage.
        Permanent blocker = addCreatureReady(player2, new CrabappleCohort());

        Permanent attacker = addCreatureReady(player1, new RustrazorButcher());
        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));
        resolveCombat();

        // Rustrazor Butcher (1 power) deals its damage as one -1/-1 counter; 4/4 becomes 3/3.
        Permanent survivor = findPermanent(player2, "Crabapple Cohort");
        assertThat(survivor.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("First strike wither kills a 1/1 blocker before it can deal damage back")
    void firstStrikeWitherKillsBlockerWithoutRetaliation() {
        Permanent attacker = addCreatureReady(player1, new RustrazorButcher());
        Permanent blocker = addCreatureReady(player2, new BriarberryCohort());
        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));
        resolveCombat();

        // One -1/-1 counter makes the 1/1 a 0/0; it dies to first strike and never deals damage.
        harness.assertInGraveyard(player2, "Briarberry Cohort");
        assertThat(attacker.getMarkedDamage()).isEqualTo(0);
    }

    @Test
    @DisplayName("Wither deals normal combat damage to a player")
    void witherDealsNormalCombatDamageToPlayer() {
        harness.setLife(player2, 20);

        Permanent attacker = addCreatureReady(player1, new RustrazorButcher());
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Opposing Butchers deal simultaneous first-strike wither damage only once")
    void opposingButchersDealSimultaneousFirstStrikeDamage() {
        Permanent attacker = addCreatureReady(player1, new RustrazorButcher());
        Permanent blocker = addCreatureReady(player2, new RustrazorButcher());
        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Rustrazor Butcher");
        harness.assertOnBattlefield(player2, "Rustrazor Butcher");
        assertThat(attacker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
