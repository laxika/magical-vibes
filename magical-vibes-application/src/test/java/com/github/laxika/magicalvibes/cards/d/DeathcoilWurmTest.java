package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BearCub;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeathcoilWurm.class, BearCub.class})
class DeathcoilWurmTest extends BaseCardTest {

    @Test
    @DisplayName("Blocked Deathcoil Wurm can assign combat damage to defending player")
    void blockedDeathcoilWurmAssignsDamageToDefendingPlayer() {
        harness.setLife(player2, 20);
        Permanent wurm = addCreatureReady(player1, new DeathcoilWurm());
        Permanent blocker = addCreatureReady(player2, new BearCub());
        wurm.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(player2.getId(), 7));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
        harness.assertOnBattlefield(player2, "Bear Cub");
    }

    @Test
    @DisplayName("Blocked Deathcoil Wurm can assign combat damage to blocker instead")
    void blockedDeathcoilWurmAssignsDamageToBlocker() {
        harness.setLife(player2, 20);
        Permanent wurm = addCreatureReady(player1, new DeathcoilWurm());
        Permanent blocker = addCreatureReady(player2, new BearCub());
        wurm.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 7));

        harness.assertNotOnBattlefield(player2, "Bear Cub");
        harness.assertInGraveyard(player2, "Bear Cub");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Unblocked Deathcoil Wurm deals combat damage to defending player")
    void unblockedDeathcoilWurmDealsCombatDamageToDefendingPlayer() {
        harness.setLife(player2, 20);
        Permanent wurm = addCreatureReady(player1, new DeathcoilWurm());
        wurm.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
    }
}
