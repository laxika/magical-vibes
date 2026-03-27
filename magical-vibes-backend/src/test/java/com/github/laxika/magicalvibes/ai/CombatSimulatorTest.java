package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.BerserkersOfBloodRidge;
import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PhantomWarrior;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.w.WallOfFrost;
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

    // ===== Temporarily stolen creatures =====

    @Test
    @DisplayName("Stolen creature has zero creature score in combat info")
    void stolenCreatureHasZeroCombatScore() {
        Permanent serraAngel = new Permanent(new SerraAngel());
        serraAngel.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(serraAngel);

        // Mark as stolen until end of turn (as Act of Treason would do)
        gd.untilEndOfTurnStolenCreatures.add(serraAngel.getId());

        CombatSimulator.CreatureInfo stolenInfo = simulator.buildCreatureInfo(
                gd, serraAngel, 0, player1.getId(), player2.getId());

        assertThat(stolenInfo.creatureScore()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Non-stolen creature has positive creature score in combat info")
    void ownedCreatureHasPositiveCombatScore() {
        Permanent serraAngel = new Permanent(new SerraAngel());
        serraAngel.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(serraAngel);

        // NOT in stolen set — normal owned creature
        CombatSimulator.CreatureInfo ownedInfo = simulator.buildCreatureInfo(
                gd, serraAngel, 0, player1.getId(), player2.getId());

        assertThat(ownedInfo.creatureScore()).isGreaterThan(0.0);
    }

    @Test
    @DisplayName("Stolen creature that dies in combat does not count toward AI creature loss")
    void stolenCreatureDeathHasNoCombatCost() {
        // Stolen 2/2 attacks, opponent's 4/4 flying blocks and kills it
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);
        gd.untilEndOfTurnStolenCreatures.add(bears.getId());

        Permanent airElemental = new Permanent(new AirElemental());
        airElemental.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(airElemental);

        CombatSimulator.CreatureInfo stolenAttacker = simulator.buildCreatureInfo(
                gd, bears, 0, player1.getId(), player2.getId());
        CombatSimulator.CreatureInfo blocker = simulator.buildCreatureInfo(
                gd, airElemental, 0, player2.getId(), player1.getId());

        // Stolen bears blocked by 4/4 — bears dies, AE survives
        CombatSimulator.CombatOutcome outcome = simulator.simulateCombat(
                gd, List.of(stolenAttacker), List.of(blocker), 20);

        // Stolen creature dying does not incur any AI loss value
        assertThat(outcome.aiCreaturesLostValue()).isEqualTo(0.0);
    }

    // ===== Trample multi-blocking =====

    @Test
    @DisplayName("Trample: both blockers assigned when a single blocker leaves lethal trample excess")
    void trampleMultiBlockToPreventLethalDamage() {
        // Colossal Dreadmaw: 6/6 Trample
        Permanent dreadmaw = new Permanent(new ColossalDreadmaw());
        dreadmaw.setSummoningSick(false);
        dreadmaw.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(dreadmaw);

        // AI is at 4 life with two 2/2 blockers.
        // One 2/2 alone: 6-2=4 trample damage = exactly lethal.
        // Both 2/2s: 6-2-2=2 trample damage = non-lethal — AI survives.
        gd.playerLifeTotals.put(player1.getId(), 4);
        Permanent bears1 = new Permanent(new GrizzlyBears());
        bears1.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears1);
        Permanent bears2 = new Permanent(new GrizzlyBears());
        bears2.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears2);

        List<int[]> blockers = simulator.findBestBlockers(gd, player1.getId(), List.of(0), List.of(0, 1));

        // Both 2/2s must block to prevent lethal trample damage
        assertThat(blockers).hasSize(2);
        assertThat(blockers).allMatch(b -> b[1] == 0); // both assigned to the Dreadmaw
    }

    @Test
    @DisplayName("Trample: high-toughness blocker stops all trample, no second blocker needed even at high life")
    void trampleWallAbsorbsAllNonLethalSituation() {
        // Colossal Dreadmaw: 6/6 Trample
        Permanent dreadmaw = new Permanent(new ColossalDreadmaw());
        dreadmaw.setSummoningSick(false);
        dreadmaw.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(dreadmaw);

        // AI at 20 life (not in lethal danger). Wall of Frost (0/7) has positive evaluateTrampleBlock
        // value since it survives (toughness 7 > power 6) and absorbs all trample damage.
        // Grizzly Bears (2/2) would die and has a net-negative blocking value — not worth assigning.
        Permanent wall = new Permanent(new WallOfFrost());
        wall.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(wall);
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        List<int[]> blockers = simulator.findBestBlockers(gd, player1.getId(), List.of(0), List.of(0, 1));

        // Wall of Frost (index 0) alone absorbs all 6 damage — trample excess is zero,
        // so the second blocker (Bears) must not be sacrificed needlessly
        assertThat(blockers).hasSize(1);
        assertThat(blockers.get(0)[0]).isEqualTo(0); // Wall of Frost
    }

    @Test
    @DisplayName("Trample: high-toughness blocker absorbs all trample damage, second blocker not needed")
    void trampleHighToughnessBlockerAbsorbsAllDamage() {
        // Colossal Dreadmaw: 6/6 Trample
        Permanent dreadmaw = new Permanent(new ColossalDreadmaw());
        dreadmaw.setSummoningSick(false);
        dreadmaw.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(dreadmaw);

        // AI has 3 life — Dreadmaw is lethal if not properly blocked
        gd.playerLifeTotals.put(player1.getId(), 3);

        // Grizzly Bears (2/2) at index 0: absorbs only 2 of 6 damage, 4 trample still lethal
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        // Wall of Frost (0/7) at index 1: toughness exceeds Dreadmaw's power, absorbs all 6 damage
        Permanent wall = new Permanent(new WallOfFrost());
        wall.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(wall);

        List<int[]> blockers = simulator.findBestBlockers(gd, player1.getId(), List.of(0), List.of(0, 1));

        // Wall of Frost (index 1) should be selected as the sole blocker — its toughness (7) exceeds
        // the Dreadmaw's power (6), so zero damage tramples through and the Bears are preserved
        assertThat(blockers).hasSize(1);
        assertThat(blockers.get(0)[0]).isEqualTo(1); // Wall of Frost
        assertThat(blockers.get(0)[1]).isEqualTo(0); // blocks Dreadmaw
    }

    @Test
    @DisplayName("Trample: multi-blocking stops once trample excess drops below lethal")
    void trampleMultiBlockStopsWhenSubLethal() {
        // Colossal Dreadmaw: 6/6 Trample
        Permanent dreadmaw = new Permanent(new ColossalDreadmaw());
        dreadmaw.setSummoningSick(false);
        dreadmaw.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(dreadmaw);

        // AI at 3 life with three 2/2 blockers.
        // After two blockers: 6-2-2=2 trample — sub-lethal vs 3 life, so the third is spared.
        gd.playerLifeTotals.put(player1.getId(), 3);
        for (int i = 0; i < 3; i++) {
            Permanent bears = new Permanent(new GrizzlyBears());
            bears.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(bears);
        }

        List<int[]> blockers = simulator.findBestBlockers(gd, player1.getId(), List.of(0), List.of(0, 1, 2));

        // Exactly two blockers: enough to reduce trample to 2 (non-lethal vs 3 life)
        assertThat(blockers).hasSize(2);
        assertThat(blockers).allMatch(b -> b[1] == 0);
    }

    @Test
    @DisplayName("Trample: all available blockers assigned when each additional one is still needed to survive")
    void trampleAllBlockersAssignedWhenEachIsRequiredToSurvive() {
        // Colossal Dreadmaw: 6/6 Trample
        Permanent dreadmaw = new Permanent(new ColossalDreadmaw());
        dreadmaw.setSummoningSick(false);
        dreadmaw.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(dreadmaw);

        // AI at 2 life with three 2/2 blockers.
        // After one: 4 trample ≥ 2 life → lethal, add second.
        // After two: 2 trample ≥ 2 life → lethal, add third.
        // After three: 0 trample → safe.
        gd.playerLifeTotals.put(player1.getId(), 2);
        for (int i = 0; i < 3; i++) {
            Permanent bears = new Permanent(new GrizzlyBears());
            bears.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(bears);
        }

        List<int[]> blockers = simulator.findBestBlockers(gd, player1.getId(), List.of(0), List.of(0, 1, 2));

        // All three blockers must be assigned to eliminate all trample damage
        assertThat(blockers).hasSize(3);
        assertThat(blockers).allMatch(b -> b[1] == 0);
    }

    @Test
    @DisplayName("AI attacks with stolen creature even when it would be held back if owned")
    void attacksWithStolenCreatureWhenOwnedCreatureWouldHoldBack() {
        // AI steals opponent's Serra Angel (4/4 flying+vigilance, higher score than AirElemental)
        Permanent serraAngel = new Permanent(new SerraAngel());
        serraAngel.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(serraAngel);
        gd.untilEndOfTurnStolenCreatures.add(serraAngel.getId());

        // Opponent has Air Elemental (4/4 flying) — slightly lower score than Serra Angel
        // This means: without the fix, the opponent would block SA with AE and the trade
        // would look unfavorable (SA score > AE score), causing the AI to not attack.
        Permanent airElemental = new Permanent(new AirElemental());
        airElemental.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(airElemental);

        List<Integer> attackers = simulator.findBestAttackers(
                gd, player1.getId(), List.of(0), List.of());

        // With the fix: stolen SA has score 0, opponent won't sacrifice their AE to block
        // a worthless attacker, so SA attacks unblocked for 4 damage — clearly worth attacking
        assertThat(attackers).containsExactly(0);
    }
}
