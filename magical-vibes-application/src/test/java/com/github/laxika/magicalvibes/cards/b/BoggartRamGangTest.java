package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CrabappleCohort;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoggartRamGang.class, GrizzlyBears.class, CrabappleCohort.class})
class BoggartRamGangTest extends BaseCardTest {

    @Test
    @DisplayName("Blocked Ram-Gang deals -1/-1 counters to the blocker instead of regular damage")
    void witherDealsMinusCountersToBlocker() {
        // Grizzly Bears is 2/2
        addCreatureReady(player2, new GrizzlyBears());

        // Boggart Ram-Gang is 3/3 with wither + haste
        Permanent attacker = addCreatureReady(player1, new BoggartRamGang());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        // Three -1/-1 counters reduce the Bears to -1/-1, so it dies.
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");

        // Ram-Gang (3/3) took only 2 regular damage and survives
        harness.assertOnBattlefield(player1, "Boggart Ram-Gang");
    }

    @Test
    @DisplayName("Unblocked Ram-Gang deals ordinary combat damage to the player")
    void witherDealsLifeLossToPlayer() {
        harness.setLife(player2, 20);

        Permanent attacker = addCreatureReady(player1, new BoggartRamGang());
        attacker.setAttacking(true);

        resolveCombat();

        // Wither only affects creatures; players take normal damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Ram-Gang can attack while summoning sick")
    void hasteAllowsAttackingImmediately() {
        harness.setLife(player2, 20);
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new BoggartRamGang());
        attacker.setSummoningSick(true);

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Blocking Ram-Gang gives a surviving attacker counters without marked damage")
    void witherAppliesWhenBlocking() {
        Permanent attacker = addCreatureReady(player1, new CrabappleCohort());
        attacker.setAttacking(true);
        addCreatureReady(player2, new BoggartRamGang());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Crabapple Cohort");
        assertThat(attacker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        assertThat(attacker.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Boggart Ram-Gang");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
