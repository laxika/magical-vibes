package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PestilentSyphoner.class})
class PestilentSyphonerTest extends BaseCardTest {

    @Test
    @DisplayName("Unblocked combat damage gives the defending player a poison counter")
    void combatDamageGivesPoisonCounter() {
        harness.setLife(player2, 20);
        Permanent syphoner = addCreatureReady(player1, new PestilentSyphoner());
        syphoner.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Blocked combat damage does not give the defending player a poison counter")
    void blockedCombatDamageGivesNoPoisonCounter() {
        Permanent syphoner = addCreatureReady(player1, new PestilentSyphoner());
        syphoner.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new PestilentSyphoner());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Toxic gives poison as combat damage is dealt without using the stack")
    void toxicDoesNotUseStack() {
        harness.setLife(player2, 20);
        Permanent syphoner = addCreatureReady(player1, new PestilentSyphoner());
        syphoner.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.resolveCombatDamage();

        harness.assertLife(player2, 19);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Increasing combat damage does not increase toxic's poison counters")
    void increasedCombatDamageStillGivesOnePoisonCounter() {
        harness.setLife(player2, 20);
        Permanent syphoner = addCreatureReady(player1, new PestilentSyphoner());
        syphoner.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        syphoner.setAttacking(true);

        resolveCombat();

        harness.assertLife(player2, 17);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }
}
