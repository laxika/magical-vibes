package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PestilentSyphonerTest extends BaseCardTest {

    @Test
    @DisplayName("Unblocked combat damage gives the defending player a poison counter")
    void combatDamageGivesPoisonCounter() {
        harness.setLife(player2, 20);
        Permanent syphoner = addReadySyphoner();
        syphoner.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Blocked combat damage does not give the defending player a poison counter")
    void blockedCombatDamageGivesNoPoisonCounter() {
        Permanent syphoner = addReadySyphoner();
        syphoner.setAttacking(true);

        Permanent blocker = new Permanent(new PestilentSyphoner());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    private Permanent addReadySyphoner() {
        Permanent perm = new Permanent(new PestilentSyphoner());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }
}
