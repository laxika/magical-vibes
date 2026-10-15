package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IchorspitBasilisk.class, HillGiant.class})
class IchorspitBasiliskTest extends BaseCardTest {

    @Test
    @DisplayName("Toxic 1 gives the defending player a poison counter on combat damage")
    void toxicDealsOnePoisonCounter() {
        harness.setLife(player2, 20);

        Permanent attacker = addCreatureReady(player1, new IchorspitBasilisk());
        attacker.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Deathtouch destroys a larger creature in combat")
    void deathtouchDestroysLargerCreature() {
        Permanent hillGiant = addCreatureReady(player1, new HillGiant());
        hillGiant.setAttacking(true);

        Permanent basilisk = addCreatureReady(player2, new IchorspitBasilisk());
        basilisk.setBlocking(true);
        basilisk.addBlockingTarget(0);

        resolveCombat();

        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertInGraveyard(player2, "Ichorspit Basilisk");
    }

    @Test
    @DisplayName("Toxic gives poison with combat damage without using the stack")
    void toxicDoesNotUseStack() {
        Permanent basilisk = addCreatureReady(player1, new IchorspitBasilisk());
        basilisk.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.resolveCombatDamage();

        harness.assertLife(player2, 19);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Increased combat damage still gives exactly one poison counter")
    void increasedDamageStillGivesOnePoisonCounter() {
        Permanent basilisk = addCreatureReady(player1, new IchorspitBasilisk());
        basilisk.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        basilisk.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.resolveCombatDamage();

        harness.assertLife(player2, 17);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Blocked combat kills the blocker with deathtouch but gives no poison")
    void blockedBasiliskDoesNotPoisonPlayer() {
        Permanent basilisk = addCreatureReady(player1, new IchorspitBasilisk());
        basilisk.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new HillGiant());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertLife(player2, 20);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
        harness.assertInGraveyard(player1, "Ichorspit Basilisk");
        harness.assertInGraveyard(player2, "Hill Giant");
    }
}
