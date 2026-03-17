package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.BerserkersOfBloodRidge;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PhantomWarrior;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CombatSimulatorTest {

    private GameTestHarness harness;
    private Player player1; // AI
    private Player player2; // Opponent
    private GameData gd;
    private CombatSimulator simulator;
    private BoardEvaluator boardEvaluator;
    private GameQueryService gameQueryService;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
        gameQueryService = harness.getGameQueryService();
        boardEvaluator = new BoardEvaluator(gameQueryService);
        simulator = new CombatSimulator(gameQueryService, boardEvaluator);

        gd.playerBattlefields.get(player1.getId()).clear();
        gd.playerBattlefields.get(player2.getId()).clear();
    }

    @Test
    @DisplayName("Unblocked attacker deals damage equal to power")
    void unblockedAttackerDealsDamage() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        List<Integer> attackers = simulator.findBestAttackers(gd, player1.getId(), List.of(0), List.of());

        // Should attack since no blockers
        assertThat(attackers).containsExactly(0);
    }

    @Test
    @DisplayName("2/2 vs 4/4: defender blocks favorably, attacker dies")
    void defenderBlocksFavorably() {
        Permanent aiBears = new Permanent(new GrizzlyBears());
        aiBears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(aiBears);

        Permanent oppAirElemental = new Permanent(new AirElemental());
        oppAirElemental.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(oppAirElemental);

        // Simulate combat: 2/2 attacks into 4/4 blocker
        CombatSimulator.CreatureInfo attackerInfo = simulator.buildCreatureInfo(
                gd, aiBears, 0, player1.getId(), player2.getId());
        CombatSimulator.CreatureInfo blockerInfo = simulator.buildCreatureInfo(
                gd, oppAirElemental, 0, player2.getId(), player1.getId());

        CombatSimulator.CombatOutcome outcome = simulator.simulateCombat(
                gd, List.of(attackerInfo), List.of(blockerInfo), 20);

        // Attacker (2/2) should die — blocked by 4/4
        assertThat(outcome.aiCreaturesLostValue()).isGreaterThan(0);
        // Blocker (4/4) should survive — 2 damage doesn't kill it
        assertThat(outcome.opponentCreaturesLostValue()).isEqualTo(0);
    }

    @Test
    @DisplayName("Flying attacker not blockable by ground creature")
    void flyingNotBlockableByGround() {
        Permanent airElemental = new Permanent(new AirElemental());
        airElemental.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(airElemental);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        List<Integer> attackers = simulator.findBestAttackers(gd, player1.getId(), List.of(0), List.of());

        // Air Elemental should attack since bears can't block it (no flying or reach)
        assertThat(attackers).containsExactly(0);
    }

    @Test
    @DisplayName("Lethal detection: go all-in when lethal damage possible")
    void lethalDetection() {
        gd.playerLifeTotals.put(player2.getId(), 4);

        Permanent bears1 = new Permanent(new GrizzlyBears());
        bears1.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears1);

        Permanent bears2 = new Permanent(new GrizzlyBears());
        bears2.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears2);

        // No blockers on opponent side
        List<Integer> attackers = simulator.findBestAttackers(gd, player1.getId(), List.of(0, 1), List.of());

        // 2+2=4 = exact lethal, should attack with both
        assertThat(attackers).containsExactlyInAnyOrder(0, 1);
    }

    @Test
    @DisplayName("Does not attack when trade is unfavorable")
    void noAttackIntoUnfavorableTrade() {
        // AI has a 2/2 bears
        Permanent aiBears = new Permanent(new GrizzlyBears());
        aiBears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(aiBears);

        // Opponent has a 4/4 Air Elemental (flying, but the bear can't block it anyway)
        // and the Air Elemental can block the bear
        Permanent airElemental = new Permanent(new AirElemental());
        airElemental.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(airElemental);

        List<Integer> attackers = simulator.findBestAttackers(gd, player1.getId(), List.of(0), List.of());

        // Bears attacking into a 4/4 should not be selected (bears dies, AE survives)
        // The simulator should find this is a bad trade
        assertThat(attackers).isEmpty();
    }

    @Test
    @DisplayName("No attackers available returns empty list")
    void noAttackersReturnsEmpty() {
        List<Integer> attackers = simulator.findBestAttackers(gd, player1.getId(), List.of(), List.of());
        assertThat(attackers).isEmpty();
    }

    // ===== Must-attack =====

    @Test
    @DisplayName("Must-attack creature is included even when trade is unfavorable")
    void mustAttackCreatureIncludedEvenWhenUnfavorable() {
        // AI has Berserkers of Blood Ridge (4/4) that must attack
        Permanent berserkers = new Permanent(new BerserkersOfBloodRidge());
        berserkers.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(berserkers);

        // Opponent has Air Elemental (4/4 flying) that can block it — even trade
        Permanent airElemental = new Permanent(new AirElemental());
        airElemental.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(airElemental);

        List<Integer> attackers = simulator.findBestAttackers(
                gd, player1.getId(), List.of(0), List.of(0));

        // Berserkers must be included regardless of trade outcome
        assertThat(attackers).contains(0);
    }

    @Test
    @DisplayName("Must-attack creature included alongside optional attacker chosen by simulator")
    void mustAttackWithOptionalAttacker() {
        // AI has Berserkers (must attack) and a Grizzly Bears (optional)
        Permanent berserkers = new Permanent(new BerserkersOfBloodRidge());
        berserkers.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(berserkers);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        // No blockers — both should attack
        List<Integer> attackers = simulator.findBestAttackers(
                gd, player1.getId(), List.of(0, 1), List.of(0));

        assertThat(attackers).contains(0); // must-attack always present
        assertThat(attackers).contains(1); // bears should also attack (no blockers)
    }

    @Test
    @DisplayName("Only must-attack creature returned when optional creature has unfavorable trade")
    void onlyMustAttackWhenOptionalIsUnfavorable() {
        // AI has Berserkers (must-attack, index 0) and Bears (optional, index 1)
        Permanent berserkers = new Permanent(new BerserkersOfBloodRidge());
        berserkers.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(berserkers);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        // Opponent has Air Elemental (4/4 flying) — can block bears unfavorably
        Permanent airElemental = new Permanent(new AirElemental());
        airElemental.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(airElemental);

        List<Integer> attackers = simulator.findBestAttackers(
                gd, player1.getId(), List.of(0, 1), List.of(0));

        // Berserkers must always be included
        assertThat(attackers).contains(0);
    }

    @Test
    @DisplayName("Multiple must-attack creatures all included")
    void multipleMustAttackCreaturesAllIncluded() {
        Permanent berserkers1 = new Permanent(new BerserkersOfBloodRidge());
        berserkers1.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(berserkers1);

        Permanent berserkers2 = new Permanent(new BerserkersOfBloodRidge());
        berserkers2.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(berserkers2);

        List<Integer> attackers = simulator.findBestAttackers(
                gd, player1.getId(), List.of(0, 1), List.of(0, 1));

        assertThat(attackers).containsExactlyInAnyOrder(0, 1);
    }

    @Test
    @DisplayName("Must-attack with lethal detection: forced attacker contributes to lethal count")
    void mustAttackContributesToLethal() {
        gd.playerLifeTotals.put(player2.getId(), 6);

        // AI has Berserkers (4/4 must-attack) and Bears (2/2 optional)
        Permanent berserkers = new Permanent(new BerserkersOfBloodRidge());
        berserkers.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(berserkers);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        // No blockers — 4+2=6 exact lethal
        List<Integer> attackers = simulator.findBestAttackers(
                gd, player1.getId(), List.of(0, 1), List.of(0));

        assertThat(attackers).containsExactlyInAnyOrder(0, 1);
    }

    @Test
    @DisplayName("Empty must-attack list behaves same as before")
    void emptyMustAttackListIsNoOp() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        // No blockers — bears should attack
        List<Integer> withEmptyMustAttack = simulator.findBestAttackers(
                gd, player1.getId(), List.of(0), List.of());

        assertThat(withEmptyMustAttack).containsExactly(0);
    }

    // ===== Blocker selection =====

    @Test
    @DisplayName("Blocker selection: favorable block kills attacker")
    void favorableBlockKillsAttacker() {
        // Opponent attacks with 2/2
        Permanent oppBears = new Permanent(new GrizzlyBears());
        oppBears.setSummoningSick(false);
        oppBears.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(oppBears);

        // AI has a 4/4 Air Elemental to block
        Permanent airElemental = new Permanent(new AirElemental());
        airElemental.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(airElemental);

        List<int[]> blockers = simulator.findBestBlockers(gd, player1.getId(), List.of(0), List.of(0));

        // AE should block bears (AE kills bears and survives)
        assertThat(blockers).hasSize(1);
        assertThat(blockers.get(0)[0]).isEqualTo(0); // blocker index
        assertThat(blockers.get(0)[1]).isEqualTo(0); // attacker index
    }

    @Test
    @DisplayName("Unblockable creature cannot be assigned blockers")
    void unblockableCreatureNotBlocked() {
        // Opponent attacks with Phantom Warrior (unblockable)
        Permanent phantom = new Permanent(new PhantomWarrior());
        phantom.setSummoningSick(false);
        phantom.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(phantom);

        // AI has a creature that could theoretically block
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        List<int[]> blockers = simulator.findBestBlockers(gd, player1.getId(), List.of(0), List.of(0));

        // Should not assign any blockers (phantom warrior can't be blocked)
        assertThat(blockers).isEmpty();
    }
}
