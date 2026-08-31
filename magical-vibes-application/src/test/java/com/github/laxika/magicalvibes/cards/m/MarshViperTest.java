package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FireWhip;
import com.github.laxika.magicalvibes.cards.s.Squire;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarshViper.class, Squire.class, FireWhip.class})
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
        Permanent aura = new Permanent(new FireWhip());
        aura.setAttachedTo(viper.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }

    @Test
    @DisplayName("No poison counters when Marsh Viper is blocked and deals no damage to a player")
    void noPoisonWhenBlocked() {
        Permanent viper = addReadyViper();
        viper.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new Squire());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(0);
    }
}
