package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HermeticStudy;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PitScorpion.class, GrizzlyBears.class, HermeticStudy.class})
class PitScorpionTest extends BaseCardTest {

    private Permanent addReadyScorpion() {
        return addCreatureReady(player1, new PitScorpion());
    }

    @Test
    @DisplayName("Dealing combat damage to a player gives that player a poison counter")
    void combatDamageGivesPoisonCounter() {
        Permanent scorpion = addReadyScorpion();
        scorpion.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Combat damage still reduces the player's life normally")
    void combatDamageStillDealsNormalDamage() {
        harness.setLife(player2, 20);
        Permanent scorpion = addReadyScorpion();
        scorpion.setAttacking(true);

        resolveCombat();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("No poison counter when Pit Scorpion is blocked and deals no damage to a player")
    void noPoisonWhenBlocked() {
        Permanent scorpion = addReadyScorpion();
        scorpion.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("Noncombat damage to a player also gives that player a poison counter")
    void noncombatDamageGivesPoisonCounter() {
        harness.setLife(player2, 20);
        Permanent scorpion = addReadyScorpion();
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HermeticStudy());
        aura.setAttachedTo(scorpion.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        harness.assertLife(player2, 19);
    }
}
