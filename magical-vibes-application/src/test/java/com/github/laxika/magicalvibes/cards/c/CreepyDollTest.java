package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.m.MahamotiDjinn;
import com.github.laxika.magicalvibes.cards.l.Lumberknot;
import com.github.laxika.magicalvibes.cards.s.SelflessCathar;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CreepyDoll.class, MahamotiDjinn.class, Lumberknot.class, SelflessCathar.class, WalkingCorpse.class})
class CreepyDollTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to creature triggers coin flip — blocker destroyed on win or survives on loss")
    void combatDamageToCreatureTriggersFlip() {
        Permanent creepyDoll = addCreatureReady(player1, new CreepyDoll());
        creepyDoll.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new MahamotiDjinn());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.passBothPriorities(); // resolve triggered ability

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("Creepy Doll's ability triggers"));
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("coin flip for Creepy Doll"));

        boolean inGraveyard = gd.playerGraveyards.get(player2.getId()).stream()
                .anyMatch(c -> c.getName().equals("Mahamoti Djinn"));
        boolean onBattlefield = gd.playerBattlefields.get(player2.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("Mahamoti Djinn"));

        if (inGraveyard) {
            // Won the flip: blocker destroyed
            assertThat(onBattlefield).isFalse();
            assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("wins the coin flip"));
        } else {
            // Lost the flip: blocker survives
            assertThat(onBattlefield).isTrue();
            assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("loses the coin flip"));
        }
    }

    @Test
    @DisplayName("Creepy Doll survives combat due to indestructible")
    void survivesCombarDueToIndestructible() {
        Permanent creepyDoll = addCreatureReady(player1, new CreepyDoll());
        creepyDoll.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new MahamotiDjinn());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.passBothPriorities(); // resolve triggered ability

        // Creepy Doll should still be on the battlefield (indestructible)
        harness.assertOnBattlefield(player1, "Creepy Doll");
        harness.assertNotInGraveyard(player1, "Creepy Doll");
    }

    @Test
    @DisplayName("No trigger fires when Creepy Doll deals combat damage to a player")
    void noTriggerOnDamageToPlayer() {
        Permanent creepyDoll = addCreatureReady(player1, new CreepyDoll());
        creepyDoll.setAttacking(true);

        resolveCombat();

        // Should deal 1 damage to player, no trigger
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).noneMatch(log -> log.contains("Creepy Doll's ability triggers"));
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).noneMatch(log -> log.contains("coin flip"));
    }

    @Test
    @DisplayName("Creepy Doll's ability also triggers when it deals damage as a blocker")
    void triggersWhileBlocking() {
        Permanent attacker = addCreatureReady(player1, new WalkingCorpse());
        attacker.setAttacking(true);
        Permanent doll = addCreatureReady(player2, new CreepyDoll());
        doll.setBlocking(true);
        doll.addBlockingTarget(0);

        harness.resolveCombatDamage();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("coin flip for Creepy Doll"));
        boolean won = gd.gameLog.stream().map(GameLogEntry::plainText)
                .anyMatch(log -> log.contains("wins the coin flip"));
        if (won) {
            harness.assertInGraveyard(player1, "Walking Corpse");
            harness.assertNotOnBattlefield(player1, "Walking Corpse");
        } else {
            harness.assertOnBattlefield(player1, "Walking Corpse");
            harness.assertNotInGraveyard(player1, "Walking Corpse");
        }
        harness.assertOnBattlefield(player2, "Creepy Doll");
    }

    @Test
    @DisplayName("The coin is still flipped when combat damage already killed the damaged creature")
    void flipsEvenWhenDamagedCreatureAlreadyDied() {
        Permanent doll = addCreatureReady(player1, new CreepyDoll());
        doll.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new SelflessCathar());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.resolveCombatDamage();
        harness.assertInGraveyard(player2, "Selfless Cathar");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("coin flip for Creepy Doll"));
        harness.assertOnBattlefield(player1, "Creepy Doll");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Hexproof does not stop the non-targeting destruction ability")
    void damagedHexproofCreatureCanBeDestroyed() {
        Permanent doll = addCreatureReady(player1, new CreepyDoll());
        doll.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new Lumberknot());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.resolveCombatDamage();
        harness.assertOnBattlefield(player2, "Lumberknot");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("coin flip for Creepy Doll"));
        boolean won = gd.gameLog.stream().map(GameLogEntry::plainText)
                .anyMatch(log -> log.contains("wins the coin flip"));
        if (won) {
            harness.assertInGraveyard(player2, "Lumberknot");
            harness.assertNotOnBattlefield(player2, "Lumberknot");
        } else {
            harness.assertOnBattlefield(player2, "Lumberknot");
            harness.assertNotInGraveyard(player2, "Lumberknot");
        }
    }

    @Test
    @DisplayName("Both dolls trigger, but neither can destroy the other")
    void damagedIndestructibleCreatureSurvivesEitherFlipResult() {
        Permanent attacker = addCreatureReady(player1, new CreepyDoll());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new CreepyDoll());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.resolveCombatDamage();
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)
                .filter(log -> log.contains("coin flip for Creepy Doll"))).hasSize(2);
        harness.assertOnBattlefield(player1, "Creepy Doll");
        harness.assertOnBattlefield(player2, "Creepy Doll");
        harness.assertNotInGraveyard(player1, "Creepy Doll");
        harness.assertNotInGraveyard(player2, "Creepy Doll");
    }
}
