package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SkyhunterSkirmisherTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Skyhunter Skirmisher has correct card properties")
    void hasCorrectProperties() {
        SkyhunterSkirmisher card = new SkyhunterSkirmisher();

        assertThat(card.getName()).isEqualTo("Skyhunter Skirmisher");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{1}{W}{W}");
        assertThat(card.getColor()).isEqualTo(CardColor.WHITE);
        assertThat(card.getPower()).isEqualTo(1);
        assertThat(card.getToughness()).isEqualTo(1);
        assertThat(card.getKeywords()).containsExactlyInAnyOrder(Keyword.FLYING, Keyword.DOUBLE_STRIKE);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.CAT, CardSubtype.KNIGHT);
    }

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

        GameData gd = harness.getGameData();
        // Bears dies from 1 + 1 = 2 total damage
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        // Skirmisher also dies from 2 regular damage (2 >= 1)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Skyhunter Skirmisher"));
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

        GameData gd = harness.getGameData();
        // Blocker killed in first strike phase
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        // Skirmisher survives — blocker was dead before it could deal damage
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Skyhunter Skirmisher"));
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

        GameData gd = harness.getGameData();
        // Skirmisher dies
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Skyhunter Skirmisher"));
        // 3/3 survives (took only 2 total damage)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
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

        GameData gd = harness.getGameData();
        // Both die in first strike phase
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Skyhunter Skirmisher"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    // ===== Helpers =====

    private Permanent addReadySkirmisher(Player player) {
        SkyhunterSkirmisher card = new SkyhunterSkirmisher();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}

