package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BearCub;
import com.github.laxika.magicalvibes.cards.o.Overrun;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeathcoilWurm.class, BearCub.class, Overrun.class})
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

    @Test
    @DisplayName("Deathcoil Wurm with trample can still assign all damage as though unblocked")
    void trampleDoesNotRequireDamageToBlockerWhenAssigningAsUnblocked() {
        harness.setLife(player2, 20);
        Permanent wurm = addCreatureReady(player1, new DeathcoilWurm());
        Permanent blocker = addCreatureReady(player2, new BearCub());

        harness.castFromHand(player1, new Overrun(), "{2}{G}{G}{G}");
        harness.passBothPriorities();

        wurm.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(player2.getId(), 10));

        harness.assertLife(player2, 10);
        harness.assertOnBattlefield(player2, "Bear Cub");
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(wurm.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Without trample Deathcoil Wurm cannot split damage between blocker and player")
    void cannotSplitDamageBetweenBlockerAndPlayer() {
        Permanent wurm = addCreatureReady(player1, new DeathcoilWurm());
        Permanent blocker = addCreatureReady(player2, new BearCub());
        wurm.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        resolveCombat();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 2, player2.getId(), 5)))
                .isInstanceOf(IllegalStateException.class);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(player2.getId(), 7));
        harness.assertLife(player2, 13);
        harness.assertOnBattlefield(player2, "Bear Cub");
    }

    @Test
    @DisplayName("All blockers still damage Deathcoil Wurm when it assigns as though unblocked")
    void assigningAsUnblockedDoesNotPreventBlockersDamage() {
        harness.setLife(player2, 20);
        Permanent wurm = addCreatureReady(player1, new DeathcoilWurm());
        Permanent firstBlocker = addCreatureReady(player2, new BearCub());
        Permanent secondBlocker = addCreatureReady(player2, new BearCub());
        wurm.setAttacking(true);
        firstBlocker.setBlocking(true);
        firstBlocker.addBlockingTarget(0);
        secondBlocker.setBlocking(true);
        secondBlocker.addBlockingTarget(0);
        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(player2.getId(), 7));

        harness.assertLife(player2, 13);
        assertThat(wurm.getMarkedDamage()).isEqualTo(4);
        assertThat(firstBlocker.getMarkedDamage()).isZero();
        assertThat(secondBlocker.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(firstBlocker, secondBlocker);
    }
}
