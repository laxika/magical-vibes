package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LeoninSquire;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkyhunterSkirmisher.class, LeoninSquire.class})
class SkyhunterSkirmisherTest extends BaseCardTest {

    // ===== Double strike deals damage in both phases =====

    @Test
    @DisplayName("Double strike deals damage twice to a blocker, totaling double power")
    void doubleStrikeDealsDamageTwiceToBlocker() {
        // Skyhunter Skirmisher (1/1 double strike) attacks, blocked by Leonin Squire (2/2)
        // Phase 1: deals 1 first-strike damage → Squire survives (1 < 2)
        // Phase 2: deals 1 regular damage → total 2 >= 2 → Squire dies
        Permanent attacker = addReadySkirmisher(player1);
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new LeoninSquire());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // Squire dies from 1 + 1 = 2 total damage
        harness.assertNotOnBattlefield(player2, "Leonin Squire");
        harness.assertInGraveyard(player2, "Leonin Squire");
        // Skirmisher also dies from 2 regular damage (2 >= 1)
        harness.assertNotOnBattlefield(player1, "Skyhunter Skirmisher");
    }

    @Test
    @DisplayName("Double strike kills 1/1 blocker in first strike phase, Skirmisher survives")
    void doubleStrikeKillsSmallBlockerInFirstStrikePhase() {
        // Skyhunter Skirmisher (1/1 double strike) attacks, blocked by a 1/1 Squire
        // Phase 1: deals 1 first-strike damage → blocker dies (1 >= 1)
        // Blocker is dead before regular damage phase → cannot deal damage back
        // Skirmisher survives
        Permanent attacker = addReadySkirmisher(player1);
        attacker.setAttacking(true);

        LeoninSquire smallCreature = new LeoninSquire();
        smallCreature.setPower(1);
        smallCreature.setToughness(1);
        Permanent blocker = addCreatureReady(player2, smallCreature);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // Blocker killed in first strike phase
        harness.assertNotOnBattlefield(player2, "Leonin Squire");
        // Skirmisher survives — blocker was dead before it could deal damage
        harness.assertOnBattlefield(player1, "Skyhunter Skirmisher");
    }

    @Test
    @DisplayName("Flying prevents a non-flying creature from blocking")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        addReadySkirmisher(player1);
        addCreatureReady(player2, new LeoninSquire());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
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

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    // ===== Double strike vs larger blocker =====

    @Test
    @DisplayName("Double strike creature dies to larger blocker that survives both phases")
    void doubleStrikeDiesToLargerBlocker() {
        // Skyhunter Skirmisher (1/1 double strike) attacks, blocked by a 3/3 Squire
        // Phase 1: deals 1 first-strike damage → 3/3 survives (1 < 3)
        // Phase 2: deals 1 more damage (total 2) → 3/3 still survives (2 < 3)
        //          3/3 deals 3 damage → Skirmisher dies (3 >= 1)
        Permanent attacker = addReadySkirmisher(player1);
        attacker.setAttacking(true);

        LeoninSquire bigCreature = new LeoninSquire();
        bigCreature.setPower(3);
        bigCreature.setToughness(3);
        Permanent blocker = addCreatureReady(player2, bigCreature);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // Skirmisher dies
        harness.assertNotOnBattlefield(player1, "Skyhunter Skirmisher");
        // 3/3 survives (took only 2 total damage)
        harness.assertOnBattlefield(player2, "Leonin Squire");
    }

    // ===== Double strike vs first strike =====

    @Test
    @DisplayName("Double strike trades with equal-power first strike creature")
    void doubleStrikeTradesWithFirstStrike() {
        // Skyhunter Skirmisher (1/1 double strike) attacks, blocked by a 1/1 first strike Squire
        // Phase 1: both deal 1 damage simultaneously → both die (1 >= 1)
        Permanent attacker = addReadySkirmisher(player1);
        attacker.setAttacking(true);

        LeoninSquire fsCreature = new LeoninSquire();
        fsCreature.setPower(1);
        fsCreature.setToughness(1);
        fsCreature.setKeywords(java.util.Set.of(Keyword.FIRST_STRIKE));
        Permanent blocker = addCreatureReady(player2, fsCreature);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // Both die in first strike phase
        harness.assertNotOnBattlefield(player1, "Skyhunter Skirmisher");
        harness.assertNotOnBattlefield(player2, "Leonin Squire");
    }

    // ===== Helpers =====

    private Permanent addReadySkirmisher(Player player) {
        return addCreatureReady(player, new SkyhunterSkirmisher());
    }
}

