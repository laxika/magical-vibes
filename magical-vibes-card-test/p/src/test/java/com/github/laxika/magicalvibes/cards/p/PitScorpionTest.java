package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HermeticStudy;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
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
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Combat damage still reduces the player's life normally")
    void combatDamageStillDealsNormalDamage() {
        harness.setLife(player2, 20);
        Permanent scorpion = addReadyScorpion();
        scorpion.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

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
        resolveAllTriggers();

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
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Damage to Pit Scorpion's controller gives that controller poison")
    void noncombatDamageToControllerGivesPoison() {
        harness.setLife(player1, 20);
        Permanent scorpion = addReadyScorpion();
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HermeticStudy());
        aura.setAttachedTo(scorpion.getId());

        harness.activateAbility(player1, 0, null, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Noncombat damage to a creature gives neither player poison")
    void noncombatDamageToCreatureDoesNotGivePoison() {
        Permanent scorpion = addReadyScorpion();
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HermeticStudy());
        aura.setAttachedTo(scorpion.getId());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bears.getId());
        resolveAllTriggers();

        assertThat(bears.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("A tenth poison counter from Pit Scorpion causes the damaged player to lose")
    void tenthPoisonCounterCausesLoss() {
        harness.setLife(player2, 20);
        gd.playerPoisonCounters.put(player2.getId(), 9);
        Permanent scorpion = addReadyScorpion();
        scorpion.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(10);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Dealing more than one damage still gives only one poison counter")
    void increasedCombatDamageGivesOnlyOnePoisonCounter() {
        harness.setLife(player2, 20);
        Permanent scorpion = addReadyScorpion();
        scorpion.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        scorpion.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 17);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }
}
