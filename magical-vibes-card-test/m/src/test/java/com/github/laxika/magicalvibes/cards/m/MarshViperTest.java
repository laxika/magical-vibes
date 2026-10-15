package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FireWhip;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarshViper.class, GrizzlyBears.class, FireWhip.class})
class MarshViperTest extends BaseCardTest {

    private Permanent addReadyViper() {
        return addCreatureReady(player1, new MarshViper());
    }

    @Test
    @DisplayName("Dealing combat damage to a player gives that player two poison counters")
    void combatDamageGivesTwoPoisonCounters() {
        Permanent viper = addReadyViper();
        viper.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }

    @Test
    @DisplayName("Combat damage still reduces the player's life normally")
    void combatDamageStillDealsNormalDamage() {
        harness.setLife(player2, 20);
        Permanent viper = addReadyViper();
        viper.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Noncombat damage to a player gives that player two poison counters")
    void noncombatDamageGivesTwoPoisonCounters() {
        harness.setLife(player2, 20);
        Permanent viper = addReadyViper();
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FireWhip());
        aura.setAttachedTo(viper.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }

    @Test
    @DisplayName("No poison counters when Marsh Viper is blocked and deals no damage to a player")
    void noPoisonWhenBlocked() {
        Permanent viper = addReadyViper();
        viper.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("Poison counters are given only when the damage trigger resolves")
    void poisonWaitsForTriggerResolution() {
        Permanent viper = addReadyViper();
        viper.setAttacking(true);

        harness.resolveCombatDamage();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Each Marsh Viper gives two poison counters independently")
    void twoVipersGiveFourPoisonCounters() {
        addReadyViper().setAttacking(true);
        addReadyViper().setAttacking(true);

        harness.resolveCombatDamage();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(4);
    }

    @Test
    @DisplayName("Damage to Marsh Viper's own controller poisons that controller")
    void noncombatDamageToControllerGivesPoison() {
        Permanent viper = addReadyViper();
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FireWhip());
        aura.setAttachedTo(viper.getId());

        harness.activateAbility(player1, 0, null, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Damage dealt by Fire Whip itself does not trigger Marsh Viper")
    void auraDamageDoesNotGivePoison() {
        Permanent viper = addReadyViper();
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FireWhip());
        aura.setAttachedTo(viper.getId());

        harness.activateAbility(player1, 1, null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }
}
