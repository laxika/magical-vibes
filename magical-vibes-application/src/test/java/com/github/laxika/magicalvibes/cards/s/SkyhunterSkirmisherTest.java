package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SkyhunterSkirmisherTest extends BaseCardTest {

    // ===== Double strike deals damage in both phases =====

    @Test
    @DisplayName("Double strike deals damage twice to a blocker, totaling double power")
    void doubleStrikeDealsDamageTwiceToBlocker() {
        // Skyhunter Skirmisher (1/1 double strike) attacks, blocked by Grizzly Bears (2/2)
        // Phase 1: deals 1 first-strike damage → Bears survives (1 < 2)
        // Phase 2: deals 1 regular damage → total 2 >= 2 → Bears dies
        Permanent attacker = addReadySkirmisher(player1);
        attacker.setAttacking(true);

        GrizzlyBears bears = new GrizzlyBears();
        Permanent blocker = new Permanent(bears);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(blocker);

        resolveCombat();

        // Bears dies from 1 + 1 = 2 total damage
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        // Skirmisher also dies from 2 regular damage (2 >= 1)
        harness.assertNotOnBattlefield(player1, "Skyhunter Skirmisher");
    }

    @Test
    @DisplayName("Double strike kills 1/1 blocker in first strike phase, Skirmisher survives")
    void doubleStrikeKillsSmallBlockerInFirstStrikePhase() {
        // Skyhunter Skirmisher (1/1 double strike) attacks, blocked by 1/1
        // Phase 1: deals 1 first-strike damage → blocker dies (1 >= 1)
        // Blocker is dead before regular damage phase → cannot deal damage back
        // Skirmisher survives
        Permanent attacker = addReadySkirmisher(player1);
        attacker.setAttacking(true);

        GrizzlyBears smallCreature = new GrizzlyBears();
        smallCreature.setPower(1);
        smallCreature.setToughness(1);
        Permanent blocker = new Permanent(smallCreature);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(blocker);

        resolveCombat();

        // Blocker killed in first strike phase
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        // Skirmisher survives — blocker was dead before it could deal damage
        harness.assertOnBattlefield(player1, "Skyhunter Skirmisher");
    }

    // ===== Double strike unblocked =====

    @Test
    @DisplayName("Unblocked double strike deals double damage to player")
    void unblockedDoubleStrikeDealsDoubleDamageToPlayer() {
        // Skyhunter Skirmisher (1/1 double strike) attacks unblocked
        // Phase 1: 1 damage to player
        // Phase 2: 1 damage to player
        // Total: 2 damage
        harness.setLife(player2, 20);
        Permanent attacker = addReadySkirmisher(player1);
        attacker.setAttacking(true);

        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    // ===== Double strike vs larger blocker =====

    @Test
    @DisplayName("Double strike creature dies to larger blocker that survives both phases")
    void doubleStrikeDiesToLargerBlocker() {
        // Skyhunter Skirmisher (1/1 double strike) attacks, blocked by 3/3
        // Phase 1: deals 1 first-strike damage → 3/3 survives (1 < 3)
        // Phase 2: deals 1 more damage (total 2) → 3/3 still survives (2 < 3)
        //          3/3 deals 3 damage → Skirmisher dies (3 >= 1)
        Permanent attacker = addReadySkirmisher(player1);
        attacker.setAttacking(true);

        GrizzlyBears bigCreature = new GrizzlyBears();
        bigCreature.setPower(3);
        bigCreature.setToughness(3);
        Permanent blocker = new Permanent(bigCreature);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(blocker);

        resolveCombat();

        // Skirmisher dies
        harness.assertNotOnBattlefield(player1, "Skyhunter Skirmisher");
        // 3/3 survives (took only 2 total damage)
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    // ===== Double strike vs first strike =====

    @Test
    @DisplayName("Double strike trades with equal-power first strike creature")
    void doubleStrikeTradesWithFirstStrike() {
        // Skyhunter Skirmisher (1/1 double strike) attacks, blocked by 1/1 first strike
        // Phase 1: both deal 1 damage simultaneously → both die (1 >= 1)
        Permanent attacker = addReadySkirmisher(player1);
        attacker.setAttacking(true);

        GrizzlyBears fsCreature = new GrizzlyBears();
        fsCreature.setPower(1);
        fsCreature.setToughness(1);
        fsCreature.setKeywords(java.util.Set.of(Keyword.FIRST_STRIKE));
        Permanent blocker = new Permanent(fsCreature);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(blocker);

        resolveCombat();

        // Both die in first strike phase
        harness.assertNotOnBattlefield(player1, "Skyhunter Skirmisher");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    // ===== Helpers =====

    private Permanent addReadySkirmisher(Player player) {
        SkyhunterSkirmisher card = new SkyhunterSkirmisher();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}

