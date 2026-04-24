package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.BerserkersOfBloodRidge;
import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GaeasProtector;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OgreResister;
import com.github.laxika.magicalvibes.cards.p.PhantomWarrior;
import com.github.laxika.magicalvibes.cards.p.PrizedUnicorn;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.w.WallOfFrost;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;

import java.util.EnumSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("scryfall")
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

    // ===== Exhaustive blocker search =====

    @Test
    @DisplayName("Exhaustive: double-block kills attacker that single blocker cannot")
    void exhaustiveDoubleBlockKillsLargeAttacker() {
        // Opponent attacks with Craw Wurm (6/4)
        Permanent crawWurm = new Permanent(new CrawWurm());
        crawWurm.setSummoningSick(false);
        crawWurm.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(crawWurm);

        // AI has three 2/2 creatures — no single one can kill a 6/4,
        // but two together have 4 power = exactly lethal vs 4 toughness
        for (int i = 0; i < 3; i++) {
            Permanent bears = new Permanent(new GrizzlyBears());
            bears.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(bears);
        }

        List<int[]> blockers = simulator.findBestBlockersExhaustive(
                gd, player1.getId(), List.of(0), List.of(0, 1, 2));

        // Exhaustive search should find that two bears double-blocking kills the 6/4
        // (combined 4 power >= 4 toughness). Two bears die but the Craw Wurm also dies.
        assertThat(blockers).hasSizeGreaterThanOrEqualTo(2);
        assertThat(blockers).allMatch(b -> b[1] == 0); // all assigned to Craw Wurm
    }

    @Test
    @DisplayName("Exhaustive: favorable block still works (single blocker kills attacker)")
    void exhaustiveSingleFavorableBlock() {
        // Opponent attacks with 2/2
        Permanent oppBears = new Permanent(new GrizzlyBears());
        oppBears.setSummoningSick(false);
        oppBears.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(oppBears);

        // AI has a 4/4 Air Elemental
        Permanent airElemental = new Permanent(new AirElemental());
        airElemental.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(airElemental);

        List<int[]> blockers = simulator.findBestBlockersExhaustive(
                gd, player1.getId(), List.of(0), List.of(0));

        // AE should block bears (kills bears and survives)
        assertThat(blockers).hasSize(1);
        assertThat(blockers.get(0)[0]).isEqualTo(0);
        assertThat(blockers.get(0)[1]).isEqualTo(0);
    }

    @Test
    @DisplayName("Exhaustive: no blocks when all trades are unfavorable and not lethal")
    void exhaustiveNoBlocksWhenUnfavorable() {
        // Opponent attacks with 4/4 Air Elemental
        Permanent airElemental = new Permanent(new AirElemental());
        airElemental.setSummoningSick(false);
        airElemental.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(airElemental);

        // AI has a 2/2 bears — blocking loses the bears and doesn't kill AE
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        // AI at 20 life, not threatened
        List<int[]> blockers = simulator.findBestBlockersExhaustive(
                gd, player1.getId(), List.of(0), List.of(0));

        // Should not chump-block when at safe life
        assertThat(blockers).isEmpty();
    }

    @Test
    @DisplayName("Exhaustive: assigns blockers optimally across two attackers")
    void exhaustiveOptimalAssignmentAcrossMultipleAttackers() {
        // Opponent attacks with two creatures:
        // Index 0: Grizzly Bears (2/2)
        // Index 1: Craw Wurm (6/4)
        Permanent oppBears = new Permanent(new GrizzlyBears());
        oppBears.setSummoningSick(false);
        oppBears.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(oppBears);

        Permanent crawWurm = new Permanent(new CrawWurm());
        crawWurm.setSummoningSick(false);
        crawWurm.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(crawWurm);

        // AI has: Air Elemental (4/4 flying, idx 0), and two 2/2 bears (idx 1, 2)
        // AE can't block ground creatures (it has flying but ground creatures can't block flyers)
        // Wait, flying creatures CAN block ground creatures. Let me reconsider.
        // Actually, flying creatures can block any creature. Only ground creatures can't block flyers.
        // So AE can block bears (kills them and survives) or Craw Wurm.
        // Best strategy: AE blocks bears (kills and survives), two bears double-block Craw Wurm (trade)
        Permanent aiAirElemental = new Permanent(new AirElemental());
        aiAirElemental.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(aiAirElemental);

        Permanent aiBears1 = new Permanent(new GrizzlyBears());
        aiBears1.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(aiBears1);

        Permanent aiBears2 = new Permanent(new GrizzlyBears());
        aiBears2.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(aiBears2);

        List<int[]> blockers = simulator.findBestBlockersExhaustive(
                gd, player1.getId(), List.of(0, 1), List.of(0, 1, 2));

        // Should assign all three blockers — killing both attackers while keeping AE alive
        // AE (idx 0) blocks Bears (attacker idx 0)
        // Bears 1+2 (idx 1,2) double-block Craw Wurm (attacker idx 1)
        assertThat(blockers).hasSize(3);

        // AE blocks the bears
        assertThat(blockers.stream().filter(b -> b[0] == 0).findFirst().orElseThrow()[1])
                .isEqualTo(0); // AE -> Bears

        // Both AI bears block the Craw Wurm
        List<int[]> crawWurmBlockers = blockers.stream().filter(b -> b[1] == 1).toList();
        assertThat(crawWurmBlockers).hasSize(2);
    }

    @Test
    @DisplayName("Exhaustive: chump blocks to survive lethal")
    void exhaustiveChumpBlocksLethal() {
        // Opponent attacks with Craw Wurm (6/4)
        Permanent crawWurm = new Permanent(new CrawWurm());
        crawWurm.setSummoningSick(false);
        crawWurm.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(crawWurm);

        // AI at 5 life with one 2/2 bears
        gd.playerLifeTotals.put(player1.getId(), 5);
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        List<int[]> blockers = simulator.findBestBlockersExhaustive(
                gd, player1.getId(), List.of(0), List.of(0));

        // Must chump-block to avoid lethal (6 damage > 5 life)
        assertThat(blockers).hasSize(1);
        assertThat(blockers.get(0)[1]).isEqualTo(0);
    }

    @Test
    @DisplayName("Exhaustive: menace attacker needs 2+ blockers")
    void exhaustiveMenaceRequiresTwoBlockers() {
        // Opponent attacks with a 3/3 menace creature
        Permanent menaceCreature = new Permanent(new GrizzlyBears());
        menaceCreature.getCard().setPower(3);
        menaceCreature.getCard().setToughness(3);
        menaceCreature.getCard().setKeywords(EnumSet.of(Keyword.MENACE));
        menaceCreature.setSummoningSick(false);
        menaceCreature.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(menaceCreature);

        // AI has only one 2/2 — can't block a menace creature alone
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        List<int[]> blockers = simulator.findBestBlockersExhaustive(
                gd, player1.getId(), List.of(0), List.of(0));

        // Single blocker can't block menace — should assign nothing
        assertThat(blockers).isEmpty();
    }

    @Test
    @DisplayName("Exhaustive: menace attacker blocked by two creatures")
    void exhaustiveMenaceBlockedByPair() {
        // Opponent attacks with a 3/3 menace creature
        Permanent menaceCreature = new Permanent(new GrizzlyBears());
        menaceCreature.getCard().setPower(3);
        menaceCreature.getCard().setToughness(3);
        menaceCreature.getCard().setKeywords(EnumSet.of(Keyword.MENACE));
        menaceCreature.setSummoningSick(false);
        menaceCreature.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(menaceCreature);

        // AI at low life with two 2/2 bears
        gd.playerLifeTotals.put(player1.getId(), 3);
        Permanent bears1 = new Permanent(new GrizzlyBears());
        bears1.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears1);
        Permanent bears2 = new Permanent(new GrizzlyBears());
        bears2.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears2);

        List<int[]> blockers = simulator.findBestBlockersExhaustive(
                gd, player1.getId(), List.of(0), List.of(0, 1));

        // Two bears should double-block the menace creature (3 damage is lethal)
        // Combined 4 power kills the 3/3, and blocking prevents 3 lethal damage
        assertThat(blockers).hasSize(2);
        assertThat(blockers).allMatch(b -> b[1] == 0);
    }

    @Test
    @DisplayName("Exhaustive: trample double-block to prevent lethal")
    void exhaustiveTrampleDoubleBlock() {
        // Same scenario as greedy trample test — verify exhaustive also handles it
        Permanent dreadmaw = new Permanent(new ColossalDreadmaw());
        dreadmaw.setSummoningSick(false);
        dreadmaw.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(dreadmaw);

        gd.playerLifeTotals.put(player1.getId(), 4);
        Permanent bears1 = new Permanent(new GrizzlyBears());
        bears1.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears1);
        Permanent bears2 = new Permanent(new GrizzlyBears());
        bears2.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears2);

        List<int[]> blockers = simulator.findBestBlockersExhaustive(
                gd, player1.getId(), List.of(0), List.of(0, 1));

        // Both needed: 6-2-2=2 trample (non-lethal vs 4 life)
        assertThat(blockers).hasSize(2);
        assertThat(blockers).allMatch(b -> b[1] == 0);
    }

    @Test
    @DisplayName("Exhaustive: unblockable creature not assigned blockers")
    void exhaustiveUnblockableNotBlocked() {
        Permanent phantom = new Permanent(new PhantomWarrior());
        phantom.setSummoningSick(false);
        phantom.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(phantom);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        List<int[]> blockers = simulator.findBestBlockersExhaustive(
                gd, player1.getId(), List.of(0), List.of(0));

        assertThat(blockers).isEmpty();
    }

    @Test
    @DisplayName("Exhaustive: empty attackers or blockers returns empty")
    void exhaustiveEmptyInputsReturnEmpty() {
        assertThat(simulator.findBestBlockersExhaustive(gd, player1.getId(), List.of(), List.of(0))).isEmpty();
        assertThat(simulator.findBestBlockersExhaustive(gd, player1.getId(), List.of(0), List.of())).isEmpty();
    }

    @Test
    @DisplayName("Exhaustive: first strike attacker kills blocker before it deals damage")
    void exhaustiveFirstStrikeAttackerKillsBlocker() {
        // Opponent attacks with a 3/3 first strike creature
        Permanent fsAttacker = new Permanent(new GrizzlyBears());
        fsAttacker.getCard().setPower(3);
        fsAttacker.getCard().setToughness(3);
        fsAttacker.getCard().setKeywords(EnumSet.of(Keyword.FIRST_STRIKE));
        fsAttacker.setSummoningSick(false);
        fsAttacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(fsAttacker);

        // AI has a 2/2 — it dies to FS before dealing damage, attacker survives.
        // This is a bad trade: AI loses blocker and attacker lives.
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        // At 20 life, not threatened — should avoid the unfavorable block
        List<int[]> blockers = simulator.findBestBlockersExhaustive(
                gd, player1.getId(), List.of(0), List.of(0));

        assertThat(blockers).isEmpty();
    }

    @Test
    @DisplayName("Exhaustive: blocker with first strike kills attacker before regular damage")
    void exhaustiveFirstStrikeBlockerKillsAttacker() {
        // Opponent attacks with a 2/2
        Permanent oppBears = new Permanent(new GrizzlyBears());
        oppBears.setSummoningSick(false);
        oppBears.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(oppBears);

        // AI has Youthful Knight (2/1 first strike) — FS deals 2 damage killing 2/2
        // before the 2/2 can deal its 2 damage back, so the knight survives
        Permanent knight = new Permanent(new YouthfulKnight());
        knight.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(knight);

        List<int[]> blockers = simulator.findBestBlockersExhaustive(
                gd, player1.getId(), List.of(0), List.of(0));

        // Should block — knight kills bears via FS and survives
        assertThat(blockers).hasSize(1);
    }

    @Test
    @DisplayName("Exhaustive: indestructible attacker cannot be killed — blocker sacrificed for nothing")
    void exhaustiveIndestructibleAttackerSurvives() {
        // Opponent attacks with a 5/5 indestructible creature
        Permanent indestructible = new Permanent(new GrizzlyBears());
        indestructible.getCard().setPower(5);
        indestructible.getCard().setToughness(5);
        indestructible.getCard().setKeywords(EnumSet.of(Keyword.INDESTRUCTIBLE));
        indestructible.setSummoningSick(false);
        indestructible.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(indestructible);

        // AI has a 4/4 flying (high creature value) — attacker kills it (5 >= 4 toughness)
        // but attacker is indestructible so we gain nothing. Losing a valuable flyer
        // to prevent 5 damage at 20 life is a bad trade.
        Permanent airElemental = new Permanent(new AirElemental());
        airElemental.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(airElemental);

        List<int[]> blockers = simulator.findBestBlockersExhaustive(
                gd, player1.getId(), List.of(0), List.of(0));

        // Should not block — losing AE (score ~22) to prevent 5 damage (score 10) is bad
        assertThat(blockers).isEmpty();
    }

    @Test
    @DisplayName("Exhaustive: indestructible blocker blocks freely without dying")
    void exhaustiveIndestructibleBlockerSurvives() {
        // Opponent attacks with Craw Wurm (6/4)
        Permanent crawWurm = new Permanent(new CrawWurm());
        crawWurm.setSummoningSick(false);
        crawWurm.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(crawWurm);

        // AI has a 1/1 indestructible creature — blocks without dying, prevents 6 damage
        Permanent indestructible = new Permanent(new GrizzlyBears());
        indestructible.getCard().setPower(1);
        indestructible.getCard().setToughness(1);
        indestructible.getCard().setKeywords(EnumSet.of(Keyword.INDESTRUCTIBLE));
        indestructible.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(indestructible);

        List<int[]> blockers = simulator.findBestBlockersExhaustive(
                gd, player1.getId(), List.of(0), List.of(0));

        // Should block — indestructible blocker prevents 6 damage risk-free
        assertThat(blockers).hasSize(1);
    }

    @Test
    @DisplayName("Exhaustive: infect attacker deals poison, not life damage")
    void exhaustiveInfectAttackerDealsPoisonWhenUnblocked() {
        // Opponent attacks with a 3/3 infect creature
        Permanent infectCreature = new Permanent(new GrizzlyBears());
        infectCreature.getCard().setPower(3);
        infectCreature.getCard().setToughness(3);
        infectCreature.getCard().setKeywords(EnumSet.of(Keyword.INFECT));
        infectCreature.setSummoningSick(false);
        infectCreature.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(infectCreature);

        // AI at 20 life but already has 8 poison — 3 more would be lethal (10 total)
        gd.playerPoisonCounters.put(player1.getId(), 8);
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        List<int[]> blockers = simulator.findBestBlockersExhaustive(
                gd, player1.getId(), List.of(0), List.of(0));

        // Must chump-block to avoid lethal poison (8+3 >= 10)
        assertThat(blockers).hasSize(1);
    }

    @Test
    @DisplayName("Exhaustive: flying attacker cannot be blocked by ground creature")
    void exhaustiveFlyingEvasion() {
        // Opponent attacks with Air Elemental (4/4 flying)
        Permanent flyer = new Permanent(new AirElemental());
        flyer.setSummoningSick(false);
        flyer.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(flyer);

        // AI has only a ground 2/2 — cannot block flying
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        // Even at 1 life, bears can't block a flyer
        gd.playerLifeTotals.put(player1.getId(), 1);

        List<int[]> blockers = simulator.findBestBlockersExhaustive(
                gd, player1.getId(), List.of(0), List.of(0));

        assertThat(blockers).isEmpty();
    }

    @Test
    @DisplayName("Exhaustive: lure forces all blockers onto lure attacker")
    void exhaustiveLureForcesBlocking() {
        // Opponent attacks with Prized Unicorn (2/2 lure) and a 2/2 bears
        Permanent unicorn = new Permanent(new PrizedUnicorn());
        unicorn.setSummoningSick(false);
        unicorn.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(unicorn);

        Permanent oppBears = new Permanent(new GrizzlyBears());
        oppBears.setSummoningSick(false);
        oppBears.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(oppBears);

        // AI has two 2/2 bears — both must block the unicorn (lure)
        Permanent aiBears1 = new Permanent(new GrizzlyBears());
        aiBears1.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(aiBears1);
        Permanent aiBears2 = new Permanent(new GrizzlyBears());
        aiBears2.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(aiBears2);

        List<int[]> blockers = simulator.findBestBlockersExhaustive(
                gd, player1.getId(), List.of(0, 1), List.of(0, 1));

        // Both AI bears forced onto the unicorn; none left for the opposing bears
        // All assignments should target the unicorn (attacker index 0)
        assertThat(blockers).hasSize(2);
        assertThat(blockers).allMatch(b -> b[1] == 0);
    }

    @Test
    @DisplayName("Exhaustive: must-block-if-able forces at least one blocker")
    void exhaustiveMustBlockIfAble() {
        // Opponent attacks with Gaea's Protector (4/2 must-block-if-able) and a 2/2
        Permanent protector = new Permanent(new GaeasProtector());
        protector.setSummoningSick(false);
        protector.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(protector);

        Permanent oppBears = new Permanent(new GrizzlyBears());
        oppBears.setSummoningSick(false);
        oppBears.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(oppBears);

        // AI has two 2/2 bears — one must block the protector
        Permanent aiBears1 = new Permanent(new GrizzlyBears());
        aiBears1.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(aiBears1);
        Permanent aiBears2 = new Permanent(new GrizzlyBears());
        aiBears2.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(aiBears2);

        List<int[]> blockers = simulator.findBestBlockersExhaustive(
                gd, player1.getId(), List.of(0, 1), List.of(0, 1));

        // At least one blocker must be assigned to the protector
        boolean protectorBlocked = blockers.stream().anyMatch(b -> b[1] == 0);
        assertThat(protectorBlocked).isTrue();
    }

    @Test
    @DisplayName("Exhaustive: one blocker picks the best attacker to block among multiple")
    void exhaustiveOneBlockerPicksBestTarget() {
        // Opponent attacks with a 2/2 (index 0) and a 1/1 (index 1)
        Permanent oppBears = new Permanent(new GrizzlyBears());
        oppBears.setSummoningSick(false);
        oppBears.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(oppBears);

        Permanent opp1_1 = new Permanent(new GrizzlyBears());
        opp1_1.getCard().setPower(1);
        opp1_1.getCard().setToughness(1);
        opp1_1.setSummoningSick(false);
        opp1_1.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(opp1_1);

        // AI has a 4/4 — should block the larger threat (2/2) since it kills both and survives
        Permanent airElemental = new Permanent(new AirElemental());
        airElemental.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(airElemental);

        List<int[]> blockers = simulator.findBestBlockersExhaustive(
                gd, player1.getId(), List.of(0, 1), List.of(0));

        // AE should block the 2/2 (higher value target — more damage prevented, attacker killed)
        assertThat(blockers).hasSize(1);
        assertThat(blockers.get(0)[1]).isEqualTo(0); // blocks the 2/2
    }

    @Test
    @DisplayName("Exhaustive: even trade 2/2 vs 2/2 is taken")
    void exhaustiveEvenTradeTaken() {
        // Opponent attacks with 2/2
        Permanent oppBears = new Permanent(new GrizzlyBears());
        oppBears.setSummoningSick(false);
        oppBears.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(oppBears);

        // AI has 2/2 — both die, but the trade prevents 2 damage
        Permanent aiBears = new Permanent(new GrizzlyBears());
        aiBears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(aiBears);

        List<int[]> blockers = simulator.findBestBlockersExhaustive(
                gd, player1.getId(), List.of(0), List.of(0));

        // Even trade: both 2/2s die. The scoring should favor this over taking 2 damage
        // because killing their creature + preventing 2 damage outweighs losing our creature
        assertThat(blockers).hasSize(1);
    }

    @Test
    @DisplayName("Exhaustive: lifelink blocker contributes to defender score")
    void exhaustiveLifelinkBlocker() {
        // Opponent attacks with 2/2
        Permanent oppBears = new Permanent(new GrizzlyBears());
        oppBears.setSummoningSick(false);
        oppBears.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(oppBears);

        // AI has a 2/2 lifelink creature — trade is enhanced by lifelink gaining life
        Permanent lifelinkCreature = new Permanent(new GrizzlyBears());
        lifelinkCreature.getCard().setKeywords(EnumSet.of(Keyword.LIFELINK));
        lifelinkCreature.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(lifelinkCreature);

        List<int[]> blockers = simulator.findBestBlockersExhaustive(
                gd, player1.getId(), List.of(0), List.of(0));

        // Should block — lifelink makes the trade more favorable
        assertThat(blockers).hasSize(1);
    }

    @Test
    @DisplayName("Exhaustive: trample with high-toughness blocker absorbs all damage")
    void exhaustiveTrampleWallAbsorbsAll() {
        // Colossal Dreadmaw: 6/6 Trample
        Permanent dreadmaw = new Permanent(new ColossalDreadmaw());
        dreadmaw.setSummoningSick(false);
        dreadmaw.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(dreadmaw);

        // AI has Wall of Frost (0/7) and Grizzly Bears (2/2)
        // Wall absorbs all 6 trample damage (toughness 7 > power 6), bears should be preserved
        Permanent wall = new Permanent(new WallOfFrost());
        wall.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(wall);
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        List<int[]> blockers = simulator.findBestBlockersExhaustive(
                gd, player1.getId(), List.of(0), List.of(0, 1));

        // Only Wall of Frost needed — bears should be preserved
        assertThat(blockers).hasSize(1);
        assertThat(blockers.get(0)[0]).isEqualTo(0); // Wall of Frost
    }

    @Test
    @DisplayName("Exhaustive: falls back to greedy when search space is too large")
    void exhaustiveFallsBackToGreedyForLargeSearchSpace() {
        // Create a scenario with many attackers and blockers to exceed MAX_BLOCKER_SEARCH_SPACE
        // 15 attackers and 15 blockers: (15+1)^15 ≈ 10^18 >> 2M limit
        for (int i = 0; i < 15; i++) {
            Permanent attacker = new Permanent(new GrizzlyBears());
            attacker.setSummoningSick(false);
            attacker.setAttacking(true);
            gd.playerBattlefields.get(player2.getId()).add(attacker);

            Permanent blocker = new Permanent(new GrizzlyBears());
            blocker.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(blocker);
        }

        List<Integer> attackerIndices = new ArrayList<>();
        List<Integer> blockerIndices = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            attackerIndices.add(i);
            blockerIndices.add(i);
        }

        // Should not throw or hang — falls back to greedy and returns a valid result
        List<int[]> blockers = simulator.findBestBlockersExhaustive(
                gd, player1.getId(), attackerIndices, blockerIndices);

        // Greedy fallback should produce even trades (2/2 vs 2/2)
        assertThat(blockers).isNotNull();
        assertThat(blockers.size()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Exhaustive: double-block preferred over two single chump blocks")
    void exhaustiveDoubleBlockPreferredOverSingleChumps() {
        // Opponent attacks with Craw Wurm (6/4) and a 2/2
        Permanent crawWurm = new Permanent(new CrawWurm());
        crawWurm.setSummoningSick(false);
        crawWurm.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(crawWurm);

        Permanent oppBears = new Permanent(new GrizzlyBears());
        oppBears.setSummoningSick(false);
        oppBears.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(oppBears);

        // AI has two 2/2 bears at low life
        gd.playerLifeTotals.put(player1.getId(), 7);
        Permanent aiBears1 = new Permanent(new GrizzlyBears());
        aiBears1.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(aiBears1);
        Permanent aiBears2 = new Permanent(new GrizzlyBears());
        aiBears2.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(aiBears2);

        List<int[]> blockers = simulator.findBestBlockersExhaustive(
                gd, player1.getId(), List.of(0, 1), List.of(0, 1));

        // Double-blocking the Craw Wurm (killing it) is better than chump-blocking each 1:1
        // because killing the 6/4 removes more value than chump-blocking both
        long crawWurmBlockerCount = blockers.stream().filter(b -> b[1] == 0).count();
        assertThat(crawWurmBlockerCount).isEqualTo(2);
    }

    @Test
    @DisplayName("Exhaustive: chump blocks when near-lethal even if trade is unfavorable")
    void exhaustiveChumpBlocksNearLethal() {
        // Opponent attacks with Ogre Resister (4/3) — too tough for a 2/2 to kill
        Permanent ogre = new Permanent(new OgreResister());
        ogre.setSummoningSick(false);
        ogre.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(ogre);

        // AI at 7 life with one 2/2 bears — not strict lethal (4 < 7) but near-lethal.
        // Taking 4 damage leaves AI at 3 life — one more attack kills.
        // The enhanced life weight when near-lethal should make chump-blocking correct
        // even though the trade is unfavorable (lose a 2/2, don't kill the 4/3).
        gd.playerLifeTotals.put(player1.getId(), 7);
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        List<int[]> blockers = simulator.findBestBlockersExhaustive(
                gd, player1.getId(), List.of(0), List.of(0));

        // Should chump-block to preserve life when near-lethal
        assertThat(blockers).hasSize(1);
        assertThat(blockers.get(0)[1]).isEqualTo(0);
    }

    @Test
    @DisplayName("Exhaustive: no chump block when life is safe despite unfavorable trade")
    void exhaustiveNoChumpBlockWhenSafe() {
        // Opponent attacks with Ogre Resister (4/3) — too tough for a 2/2 to kill
        Permanent ogre = new Permanent(new OgreResister());
        ogre.setSummoningSick(false);
        ogre.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(ogre);

        // AI at 20 life with one 2/2 bears — taking 4 damage leaves 16 life, very safe.
        // Pressure ratio = 4/20 = 0.2 (below 0.5 threshold), so life weight stays at 2.0.
        // Bears score (9.0) > 4 damage * 2.0 weight (8.0), so don't sacrifice the creature.
        gd.playerLifeTotals.put(player1.getId(), 20);
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        List<int[]> blockers = simulator.findBestBlockersExhaustive(
                gd, player1.getId(), List.of(0), List.of(0));

        // Should NOT chump-block: life is safe, preserve the creature
        assertThat(blockers).isEmpty();
    }

    // ===== Opponent trick risk (playing around combat tricks) =====

    @Test
    @DisplayName("Trick risk: no penalty when opponent has no threat")
    void trickRiskZeroWhenNoThreat() {
        Permanent aiBears = new Permanent(new GrizzlyBears());
        aiBears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(aiBears);

        Permanent oppBears = new Permanent(new GrizzlyBears());
        oppBears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(oppBears);

        CombatSimulator.CreatureInfo attacker = simulator.buildCreatureInfo(
                gd, aiBears, 0, player1.getId(), player2.getId());
        CombatSimulator.CreatureInfo blocker = simulator.buildCreatureInfo(
                gd, oppBears, 0, player2.getId(), player1.getId());

        double risk = simulator.computeAttackTrickRisk(
                gd, List.of(attacker), List.of(blocker),
                OpponentThreatEstimator.ThreatEstimate.NONE);

        assertThat(risk).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Trick risk: positive penalty when attacker is vulnerable to pump")
    void trickRiskPositiveWhenVulnerableToPump() {
        // AI 3/3 attacks into opponent's 2/2.
        // Normally: AI kills blocker, survives. With +3/+3 pump, blocker becomes 5/5:
        // blocker survives (3 < 5), blocker kills attacker (5 >= 3). Big swing.
        Permanent ai3_3 = new Permanent(new GrizzlyBears());
        ai3_3.getCard().setPower(3);
        ai3_3.getCard().setToughness(3);
        ai3_3.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(ai3_3);

        Permanent opp2_2 = new Permanent(new GrizzlyBears());
        opp2_2.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(opp2_2);

        CombatSimulator.CreatureInfo attacker = simulator.buildCreatureInfo(
                gd, ai3_3, 0, player1.getId(), player2.getId());
        CombatSimulator.CreatureInfo blocker = simulator.buildCreatureInfo(
                gd, opp2_2, 0, player2.getId(), player1.getId());

        // Simulate opponent having a +3/+3 pump at 30% probability
        var threat = new OpponentThreatEstimator.ThreatEstimate(0.30, 3);

        double risk = simulator.computeAttackTrickRisk(
                gd, List.of(attacker), List.of(blocker), threat);

        // Risk should be positive and substantial — pump flips a winning combat into a losing one
        assertThat(risk).isGreaterThan(0.0);
    }

    @Test
    @DisplayName("Trick risk: unblockable attacker has no pump vulnerability")
    void trickRiskZeroForUnblockable() {
        Permanent phantom = new Permanent(new PhantomWarrior());
        phantom.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(phantom);

        Permanent oppBears = new Permanent(new GrizzlyBears());
        oppBears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(oppBears);

        CombatSimulator.CreatureInfo attacker = simulator.buildCreatureInfo(
                gd, phantom, 0, player1.getId(), player2.getId());
        CombatSimulator.CreatureInfo blocker = simulator.buildCreatureInfo(
                gd, oppBears, 0, player2.getId(), player1.getId());

        var threat = new OpponentThreatEstimator.ThreatEstimate(0.40, 3);

        double risk = simulator.computeAttackTrickRisk(
                gd, List.of(attacker), List.of(blocker), threat);

        assertThat(risk).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Trick risk: attacker already losing to blocker has zero vulnerability")
    void trickRiskZeroWhenAttackerAlreadyLoses() {
        // AI 2/2 into opponent's 4/4 — attacker already dies, pump doesn't make it worse
        Permanent aiBears = new Permanent(new GrizzlyBears());
        aiBears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(aiBears);

        Permanent oppAE = new Permanent(new AirElemental());
        oppAE.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(oppAE);

        CombatSimulator.CreatureInfo attacker = simulator.buildCreatureInfo(
                gd, aiBears, 0, player1.getId(), player2.getId());
        CombatSimulator.CreatureInfo blocker = simulator.buildCreatureInfo(
                gd, oppAE, 0, player2.getId(), player1.getId());

        var threat = new OpponentThreatEstimator.ThreatEstimate(0.40, 3);

        double risk = simulator.computeAttackTrickRisk(
                gd, List.of(attacker), List.of(blocker), threat);

        // Attacker dies regardless — pump on blocker doesn't change the outcome
        assertThat(risk).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Trick risk: high pump flips a clean win into attacker death")
    void trickRiskHighWhenPumpFlipsCombat() {
        // AI 5/5 attacks into opponent's 3/3.
        // Normal: AI kills blocker (5 >= 3), AI survives (3 < 5). Clean win.
        // With +3/+3: blocker becomes 6/6 — AI dies (6 >= 5), blocker survives (5 < 6).
        // Complete reversal: from winning to losing.
        Permanent ai5_5 = new Permanent(new GrizzlyBears());
        ai5_5.getCard().setPower(5);
        ai5_5.getCard().setToughness(5);
        ai5_5.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(ai5_5);

        Permanent opp3_3 = new Permanent(new GrizzlyBears());
        opp3_3.getCard().setPower(3);
        opp3_3.getCard().setToughness(3);
        opp3_3.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(opp3_3);

        CombatSimulator.CreatureInfo attacker = simulator.buildCreatureInfo(
                gd, ai5_5, 0, player1.getId(), player2.getId());
        CombatSimulator.CreatureInfo blocker = simulator.buildCreatureInfo(
                gd, opp3_3, 0, player2.getId(), player1.getId());

        var threat = new OpponentThreatEstimator.ThreatEstimate(0.30, 3);

        double risk = simulator.computeAttackTrickRisk(
                gd, List.of(attacker), List.of(blocker), threat);

        // Risk should be significant — a 5/5 dying is a big swing
        assertThat(risk).isGreaterThan(2.0);
    }

    @Test
    @DisplayName("Trick risk: higher probability produces higher risk")
    void trickRiskScalesWithProbability() {
        Permanent ai3_3 = new Permanent(new GrizzlyBears());
        ai3_3.getCard().setPower(3);
        ai3_3.getCard().setToughness(3);
        ai3_3.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(ai3_3);

        Permanent opp2_2 = new Permanent(new GrizzlyBears());
        opp2_2.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(opp2_2);

        CombatSimulator.CreatureInfo attacker = simulator.buildCreatureInfo(
                gd, ai3_3, 0, player1.getId(), player2.getId());
        CombatSimulator.CreatureInfo blocker = simulator.buildCreatureInfo(
                gd, opp2_2, 0, player2.getId(), player1.getId());

        double riskLow = simulator.computeAttackTrickRisk(
                gd, List.of(attacker), List.of(blocker),
                new OpponentThreatEstimator.ThreatEstimate(0.10, 3));

        double riskHigh = simulator.computeAttackTrickRisk(
                gd, List.of(attacker), List.of(blocker),
                new OpponentThreatEstimator.ThreatEstimate(0.40, 3));

        assertThat(riskHigh).isGreaterThan(riskLow);
    }

    @Test
    @DisplayName("Trick risk: lethal attack still chosen despite trick risk")
    void trickRiskDoesNotPreventLethalAttack() {
        // Opponent at 4 life, AI has two 2/2 unblockable creatures — lethal
        gd.playerLifeTotals.put(player2.getId(), 4);

        Permanent phantom1 = new Permanent(new PhantomWarrior());
        phantom1.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(phantom1);

        Permanent phantom2 = new Permanent(new PhantomWarrior());
        phantom2.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(phantom2);

        // Opponent has blockers and high threat — but attackers are unblockable
        Permanent oppBears = new Permanent(new GrizzlyBears());
        oppBears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(oppBears);

        var threat = new OpponentThreatEstimator.ThreatEstimate(0.50, 4);

        List<Integer> attackers = simulator.findBestAttackers(
                gd, player1.getId(), List.of(0, 1), List.of(), threat);

        // Lethal shortcut fires before trick risk is applied — must attack with both
        assertThat(attackers).containsExactlyInAnyOrder(0, 1);
    }

    @Test
    @DisplayName("Trick risk: discourages marginal attack into open mana")
    void trickRiskDiscouragesMarginalAttack() {
        // AI 3/3 attacks into opponent's 2/3.
        // Normal: AI kills blocker (3 >= 3), blocker doesn't kill AI (2 < 3). Good attack.
        // With +3/+3: blocker becomes 5/6, kills attacker (5 >= 3), survives (3 < 6). Bad.
        // The trick risk penalty should make this attack score negative.
        Permanent ai3_3 = new Permanent(new GrizzlyBears());
        ai3_3.getCard().setPower(3);
        ai3_3.getCard().setToughness(3);
        ai3_3.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(ai3_3);

        Permanent opp2_3 = new Permanent(new GrizzlyBears());
        opp2_3.getCard().setPower(2);
        opp2_3.getCard().setToughness(3);
        opp2_3.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(opp2_3);

        // Without threat: AI attacks (good trade — kill their 2/3, survive)
        List<Integer> attackersNoThreat = simulator.findBestAttackers(
                gd, player1.getId(), List.of(0), List.of(),
                OpponentThreatEstimator.ThreatEstimate.NONE);
        assertThat(attackersNoThreat).containsExactly(0);

        // With high threat: trick risk should discourage the attack
        var highThreat = new OpponentThreatEstimator.ThreatEstimate(0.50, 3);
        List<Integer> attackersWithThreat = simulator.findBestAttackers(
                gd, player1.getId(), List.of(0), List.of(), highThreat);
        assertThat(attackersWithThreat).isEmpty();
    }

    // ===== Block trick risk tests =====

    @Test
    @DisplayName("Block trick risk: no penalty when opponent has no threat")
    void blockTrickRiskZeroWhenNoThreat() {
        // Simple block scenario, but threat estimate is NONE — risk should be 0.
        Permanent oppAttacker = new Permanent(new GrizzlyBears());
        oppAttacker.setSummoningSick(false);
        oppAttacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(oppAttacker);

        Permanent aiBlocker = new Permanent(new GrizzlyBears());
        aiBlocker.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(aiBlocker);

        CombatSimulator.CreatureInfo attackerInfo = simulator.buildCreatureInfo(
                gd, oppAttacker, 0, player2.getId(), player1.getId());
        CombatSimulator.CreatureInfo blockerInfo = simulator.buildCreatureInfo(
                gd, aiBlocker, 0, player1.getId(), player2.getId());

        List<List<CombatSimulator.CreatureInfo>> assignments = new ArrayList<>();
        assignments.add(new ArrayList<>(List.of(blockerInfo)));

        double risk = simulator.computeBlockTrickRisk(
                List.of(attackerInfo), assignments, 20, 0,
                OpponentThreatEstimator.ThreatEstimate.NONE);

        assertThat(risk).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Block trick risk: positive penalty when pump flips a profitable block")
    void blockTrickRiskPositiveWhenPumpFlipsBlock() {
        // AI 3/3 blocks opponent's 2/3. Without trick: attacker dies (3 >= 3),
        // blocker survives (2 < 3) — profitable block. With +3/+3: attacker becomes
        // 5/6, blocker dies (3 tough <= 5 power), attacker survives (3 power < 6).
        // Big swing.
        Permanent opp2_3 = new Permanent(new GrizzlyBears());
        opp2_3.getCard().setPower(2);
        opp2_3.getCard().setToughness(3);
        opp2_3.setSummoningSick(false);
        opp2_3.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(opp2_3);

        Permanent ai3_3 = new Permanent(new GrizzlyBears());
        ai3_3.getCard().setPower(3);
        ai3_3.getCard().setToughness(3);
        ai3_3.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(ai3_3);

        CombatSimulator.CreatureInfo attackerInfo = simulator.buildCreatureInfo(
                gd, opp2_3, 0, player2.getId(), player1.getId());
        CombatSimulator.CreatureInfo blockerInfo = simulator.buildCreatureInfo(
                gd, ai3_3, 0, player1.getId(), player2.getId());

        List<List<CombatSimulator.CreatureInfo>> assignments = new ArrayList<>();
        assignments.add(new ArrayList<>(List.of(blockerInfo)));

        var threat = new OpponentThreatEstimator.ThreatEstimate(0.30, 3);

        double risk = simulator.computeBlockTrickRisk(
                List.of(attackerInfo), assignments, 20, 0, threat);

        assertThat(risk).isGreaterThan(0.0);
    }

    @Test
    @DisplayName("Block trick risk: no penalty when no blockers are assigned")
    void blockTrickRiskZeroWhenNoBlocks() {
        // Attacker exists but we don't block — pump threats on unblocked attackers
        // don't affect the relative merit of the "no block" choice.
        Permanent oppAttacker = new Permanent(new GrizzlyBears());
        oppAttacker.setSummoningSick(false);
        oppAttacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(oppAttacker);

        CombatSimulator.CreatureInfo attackerInfo = simulator.buildCreatureInfo(
                gd, oppAttacker, 0, player2.getId(), player1.getId());

        List<List<CombatSimulator.CreatureInfo>> assignments = new ArrayList<>();
        assignments.add(new ArrayList<>()); // no blockers assigned

        var threat = new OpponentThreatEstimator.ThreatEstimate(0.50, 4);

        double risk = simulator.computeBlockTrickRisk(
                List.of(attackerInfo), assignments, 20, 0, threat);

        assertThat(risk).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Block pessimism: AI avoids a block that a pump would flip into a disaster")
    void blockPessimismSkipsRiskyBlock() {
        // AI 5/5 could block opponent's 2/3. Without threat: profitable block
        // (blocker kills attacker and survives untouched). With a high pump threat:
        // a +3/+3 on the attacker turns it into a 5/6, which kills our valuable 5/5
        // while surviving. The AI should decline the block rather than risk its
        // bigger creature for the 2-damage savings it would otherwise gain.
        gd.playerLifeTotals.put(player1.getId(), 20);

        Permanent opp2_3 = new Permanent(new GrizzlyBears());
        opp2_3.getCard().setPower(2);
        opp2_3.getCard().setToughness(3);
        opp2_3.setSummoningSick(false);
        opp2_3.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(opp2_3);

        Permanent ai5_5 = new Permanent(new GrizzlyBears());
        ai5_5.getCard().setPower(5);
        ai5_5.getCard().setToughness(5);
        ai5_5.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(ai5_5);

        // Without threat: AI blocks (profitable — kill their 2/3, keep our 5/5 untouched)
        List<int[]> blocksNoThreat = simulator.findBestBlockersExhaustive(
                gd, player1.getId(), List.of(0), List.of(0),
                OpponentThreatEstimator.ThreatEstimate.NONE);
        assertThat(blocksNoThreat).hasSize(1);

        // With high threat: AI declines the block rather than risk its 5/5 for the
        // ~2 damage it would save.
        var highThreat = new OpponentThreatEstimator.ThreatEstimate(0.50, 3);
        List<int[]> blocksWithThreat = simulator.findBestBlockersExhaustive(
                gd, player1.getId(), List.of(0), List.of(0), highThreat);
        assertThat(blocksWithThreat).isEmpty();
    }

    @Test
    @DisplayName("Block pessimism: AI still blocks when incoming damage is lethal")
    void blockPessimismStillBlocksLethal() {
        // AI at 2 life, opponent's 3/3 attacking — must block even if risky,
        // because not blocking is certain death.
        gd.playerLifeTotals.put(player1.getId(), 2);

        Permanent opp3_3 = new Permanent(new GrizzlyBears());
        opp3_3.getCard().setPower(3);
        opp3_3.getCard().setToughness(3);
        opp3_3.setSummoningSick(false);
        opp3_3.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(opp3_3);

        Permanent aiBlocker = new Permanent(new GrizzlyBears());
        aiBlocker.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(aiBlocker);

        var highThreat = new OpponentThreatEstimator.ThreatEstimate(0.50, 4);
        List<int[]> blocks = simulator.findBestBlockersExhaustive(
                gd, player1.getId(), List.of(0), List.of(0), highThreat);

        // Blocking is forced — chump to survive
        assertThat(blocks).hasSize(1);
    }

    // ===== Defensive value penalty tests =====

    @Test
    @DisplayName("Defensive value: AI 3/3 holds back against opponent's 5/5")
    void defensiveValueHoldsBackLastBlocker() {
        // The scenario from the task description: AI has a single 3/3, opponent
        // has a single 5/5. Attacking for 3 is correct if we can still block,
        // but since our 3/3 is our only blocker and attacking taps it, the
        // opponent's 5/5 comes through for 5 while we deal 3 to their face.
        // Holding back lets us chump-block the 5/5 next turn, taking 0 damage
        // instead of 5 — the defensive penalty should favor not attacking.
        gd.playerLifeTotals.put(player1.getId(), 6);
        gd.playerLifeTotals.put(player2.getId(), 20);

        Permanent ai3_3 = new Permanent(new GrizzlyBears());
        ai3_3.getCard().setPower(3);
        ai3_3.getCard().setToughness(3);
        ai3_3.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(ai3_3);

        Permanent opp5_5 = new Permanent(new GrizzlyBears());
        opp5_5.getCard().setPower(5);
        opp5_5.getCard().setToughness(5);
        opp5_5.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(opp5_5);

        List<Integer> attackers = simulator.findBestAttackers(
                gd, player1.getId(), List.of(0), List.of());

        // The 3/3 is our only blocker against a lethal 5/5. Attacking gives up
        // 5 damage next turn (and dies to the counter-attack); holding back
        // trades the 3/3 for their 5/5 (or at least chump-blocks it).
        assertThat(attackers).isEmpty();
    }

    @Test
    @DisplayName("Defensive value: AI still attacks when opponent has no counter-attack")
    void defensiveValueAllowsAttackWhenOpponentHasNoBoard() {
        // Same 3/3 AI creature, but opponent has no creatures. No defensive
        // value to preserve — the attack should go through.
        Permanent ai3_3 = new Permanent(new GrizzlyBears());
        ai3_3.getCard().setPower(3);
        ai3_3.getCard().setToughness(3);
        ai3_3.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(ai3_3);

        List<Integer> attackers = simulator.findBestAttackers(
                gd, player1.getId(), List.of(0), List.of());

        assertThat(attackers).containsExactly(0);
    }

    @Test
    @DisplayName("Defensive value: vigilance attacker ignores defensive penalty")
    void defensiveValueVigilanceCanAttackFreely() {
        // Serra Angel (4/4 flying, vigilance) attacks — vigilance means the
        // angel stays untapped and is still available to block next turn,
        // so the defensive penalty must not discourage the attack.
        gd.playerLifeTotals.put(player1.getId(), 6);

        Permanent aiAngel = new Permanent(new SerraAngel());
        aiAngel.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(aiAngel);

        // Opponent has a 5/5 threatening lethal
        Permanent opp5_5 = new Permanent(new GrizzlyBears());
        opp5_5.getCard().setPower(5);
        opp5_5.getCard().setToughness(5);
        opp5_5.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(opp5_5);

        List<Integer> attackers = simulator.findBestAttackers(
                gd, player1.getId(), List.of(0), List.of());

        // Vigilance means the angel can attack AND block, so sending it is safe.
        assertThat(attackers).containsExactly(0);
    }

    @Test
    @DisplayName("Defensive value: unblockable flyer still attacks while ground blocker stays back")
    void defensiveValueAttackWithSpareBlockers() {
        // AI has an Air Elemental (4/4 flying) and a 3/3 ground creature.
        // Opponent has a 5/5 ground creature (no flying/reach), which threatens
        // 5 damage next turn. The flyer is unblockable and profitable to attack;
        // the 3/3 should stay back to (chump-)block the counter-attack.
        gd.playerLifeTotals.put(player1.getId(), 6);

        Permanent aiFlyer = new Permanent(new AirElemental());
        aiFlyer.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(aiFlyer);

        Permanent ai3_3 = new Permanent(new GrizzlyBears());
        ai3_3.getCard().setPower(3);
        ai3_3.getCard().setToughness(3);
        ai3_3.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(ai3_3);

        Permanent opp5_5 = new Permanent(new GrizzlyBears());
        opp5_5.getCard().setPower(5);
        opp5_5.getCard().setToughness(5);
        opp5_5.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(opp5_5);

        List<Integer> attackers = simulator.findBestAttackers(
                gd, player1.getId(), List.of(0, 1), List.of());

        // Flyer (index 0) is unblockable and can safely attack; the 3/3 holds back.
        assertThat(attackers).containsExactly(0);
    }

    @Test
    @DisplayName("Defensive value: computePenalty returns zero when opponent has no board")
    void computeDefensivePenaltyZeroWithNoOpponent() {
        Permanent ai3_3 = new Permanent(new GrizzlyBears());
        ai3_3.getCard().setPower(3);
        ai3_3.getCard().setToughness(3);
        ai3_3.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(ai3_3);

        CombatSimulator.CreatureInfo attacker = simulator.buildCreatureInfo(
                gd, ai3_3, 0, player1.getId(), player2.getId());

        double penalty = simulator.computeDefensiveValuePenalty(
                gd, List.of(attacker), List.of(attacker), List.of(), null, 20);

        assertThat(penalty).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Defensive value: computePenalty returns zero when all attackers have vigilance")
    void computeDefensivePenaltyZeroWithVigilance() {
        Permanent aiAngel = new Permanent(new SerraAngel());
        aiAngel.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(aiAngel);

        Permanent opp5_5 = new Permanent(new GrizzlyBears());
        opp5_5.getCard().setPower(5);
        opp5_5.getCard().setToughness(5);
        opp5_5.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(opp5_5);

        CombatSimulator.CreatureInfo angelInfo = simulator.buildCreatureInfo(
                gd, aiAngel, 0, player1.getId(), player2.getId());
        CombatSimulator.CreatureInfo oppInfo = simulator.buildCreatureInfo(
                gd, opp5_5, 0, player2.getId(), player1.getId());

        double[] baselineOutcome = simulator.estimateCounterAttackOutcome(
                gd, List.of(oppInfo), List.of(angelInfo), 20);
        var baseline = new CombatSimulator.DefensiveBaseline(baselineOutcome[0], baselineOutcome[1]);

        double penalty = simulator.computeDefensiveValuePenalty(
                gd, List.of(angelInfo), List.of(angelInfo), List.of(oppInfo), baseline, 20);

        // Angel has vigilance: attacking doesn't tap it, so there's nothing to penalize.
        assertThat(penalty).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Defensive value: penalty scales with extra damage taken")
    void computeDefensivePenaltyPositiveWhenDefenseWorsens() {
        // AI at 5 life, 3/3 blocker vs opp 5/5. At such low life the 5/5's
        // attack is lethal, so the AI chump-blocks (loses the 3/3, takes 0
        // damage). If the 3/3 attacks, no blocker remains — the 5/5 lands
        // lethal damage: a large penalty must result.
        gd.playerLifeTotals.put(player1.getId(), 5);

        Permanent ai3_3 = new Permanent(new GrizzlyBears());
        ai3_3.getCard().setPower(3);
        ai3_3.getCard().setToughness(3);
        ai3_3.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(ai3_3);

        Permanent opp5_5 = new Permanent(new GrizzlyBears());
        opp5_5.getCard().setPower(5);
        opp5_5.getCard().setToughness(5);
        opp5_5.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(opp5_5);

        CombatSimulator.CreatureInfo aiInfo = simulator.buildCreatureInfo(
                gd, ai3_3, 0, player1.getId(), player2.getId());
        CombatSimulator.CreatureInfo oppInfo = simulator.buildCreatureInfo(
                gd, opp5_5, 0, player2.getId(), player1.getId());

        double[] baselineOutcome = simulator.estimateCounterAttackOutcome(
                gd, List.of(oppInfo), List.of(aiInfo), 5);
        var baseline = new CombatSimulator.DefensiveBaseline(baselineOutcome[0], baselineOutcome[1]);

        double penalty = simulator.computeDefensiveValuePenalty(
                gd, List.of(aiInfo), List.of(aiInfo), List.of(oppInfo), baseline, 5);

        // Sending the 3/3 in forfeits the lethal-chump — a very large penalty
        // (including the lethal-flip sentinel) must result.
        assertThat(penalty).isGreaterThan(500.0);
    }

    // ===== Menace interaction with mandatory-block effects =====
    // These guard the slot-based allocation: a menace attacker must have 0 or ≥ 2
    // blockers, even when lure or must-block-if-able would otherwise force exactly 1.

    @Test
    @DisplayName("Greedy: lure+menace with a single able blocker assigns zero blockers")
    void greedyLureMenaceSingleCandidateSkipped() {
        Permanent unicorn = new Permanent(new PrizedUnicorn());
        unicorn.getCard().setKeywords(EnumSet.of(Keyword.MENACE));
        unicorn.setSummoningSick(false);
        unicorn.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(unicorn);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        List<int[]> blockers = simulator.findBestBlockers(
                gd, player1.getId(), List.of(0), List.of(0));

        // Lone blocker can't legally block a menace attacker, so lure doesn't force it.
        assertThat(blockers).isEmpty();
    }

    @Test
    @DisplayName("Greedy: lure+menace with two able blockers forces both to block")
    void greedyLureMenaceTwoCandidatesBothBlock() {
        Permanent unicorn = new Permanent(new PrizedUnicorn());
        unicorn.getCard().setKeywords(EnumSet.of(Keyword.MENACE));
        unicorn.setSummoningSick(false);
        unicorn.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(unicorn);

        Permanent bears1 = new Permanent(new GrizzlyBears());
        bears1.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears1);
        Permanent bears2 = new Permanent(new GrizzlyBears());
        bears2.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears2);

        List<int[]> blockers = simulator.findBestBlockers(
                gd, player1.getId(), List.of(0), List.of(0, 1));

        assertThat(blockers).hasSize(2);
        assertThat(blockers).allMatch(b -> b[1] == 0);
    }

    @Test
    @DisplayName("Greedy: must-block-if-able + menace with one candidate assigns zero blockers")
    void greedyMustBlockMenaceSingleCandidateSkipped() {
        Permanent protector = new Permanent(new GaeasProtector());
        protector.getCard().setKeywords(EnumSet.of(Keyword.MENACE));
        protector.setSummoningSick(false);
        protector.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(protector);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        List<int[]> blockers = simulator.findBestBlockers(
                gd, player1.getId(), List.of(0), List.of(0));

        // Cannot satisfy both "must be blocked" and menace with one creature, so no block.
        assertThat(blockers).isEmpty();
    }

    @Test
    @DisplayName("Greedy: must-block-if-able + menace with two candidates assigns a pair")
    void greedyMustBlockMenaceTwoCandidatesAssignsPair() {
        Permanent protector = new Permanent(new GaeasProtector());
        protector.getCard().setKeywords(EnumSet.of(Keyword.MENACE));
        protector.setSummoningSick(false);
        protector.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(protector);

        Permanent bears1 = new Permanent(new GrizzlyBears());
        bears1.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears1);
        Permanent bears2 = new Permanent(new GrizzlyBears());
        bears2.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears2);

        List<int[]> blockers = simulator.findBestBlockers(
                gd, player1.getId(), List.of(0), List.of(0, 1));

        assertThat(blockers).hasSize(2);
        assertThat(blockers).allMatch(b -> b[1] == 0);
    }

    @Test
    @DisplayName("Exhaustive: lure+menace with a single able blocker assigns zero blockers")
    void exhaustiveLureMenaceSingleCandidateSkipped() {
        Permanent unicorn = new Permanent(new PrizedUnicorn());
        unicorn.getCard().setKeywords(EnumSet.of(Keyword.MENACE));
        unicorn.setSummoningSick(false);
        unicorn.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(unicorn);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        List<int[]> blockers = simulator.findBestBlockersExhaustive(
                gd, player1.getId(), List.of(0), List.of(0));

        assertThat(blockers).isEmpty();
    }

    @Test
    @DisplayName("Exhaustive: lure+menace with two able blockers forces both to block")
    void exhaustiveLureMenaceTwoCandidatesBothBlock() {
        Permanent unicorn = new Permanent(new PrizedUnicorn());
        unicorn.getCard().setKeywords(EnumSet.of(Keyword.MENACE));
        unicorn.setSummoningSick(false);
        unicorn.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(unicorn);

        Permanent bears1 = new Permanent(new GrizzlyBears());
        bears1.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears1);
        Permanent bears2 = new Permanent(new GrizzlyBears());
        bears2.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears2);

        List<int[]> blockers = simulator.findBestBlockersExhaustive(
                gd, player1.getId(), List.of(0), List.of(0, 1));

        assertThat(blockers).hasSize(2);
        assertThat(blockers).allMatch(b -> b[1] == 0);
    }

    @Test
    @DisplayName("Exhaustive: must-block-if-able + menace with one candidate assigns zero blockers")
    void exhaustiveMustBlockMenaceSingleCandidateSkipped() {
        Permanent protector = new Permanent(new GaeasProtector());
        protector.getCard().setKeywords(EnumSet.of(Keyword.MENACE));
        protector.setSummoningSick(false);
        protector.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(protector);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        List<int[]> blockers = simulator.findBestBlockersExhaustive(
                gd, player1.getId(), List.of(0), List.of(0));

        assertThat(blockers).isEmpty();
    }

    @Test
    @DisplayName("Exhaustive: must-block-if-able + menace with two candidates assigns a pair")
    void exhaustiveMustBlockMenaceTwoCandidatesAssignsPair() {
        Permanent protector = new Permanent(new GaeasProtector());
        protector.getCard().setKeywords(EnumSet.of(Keyword.MENACE));
        protector.setSummoningSick(false);
        protector.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(protector);

        Permanent bears1 = new Permanent(new GrizzlyBears());
        bears1.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears1);
        Permanent bears2 = new Permanent(new GrizzlyBears());
        bears2.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears2);

        List<int[]> blockers = simulator.findBestBlockersExhaustive(
                gd, player1.getId(), List.of(0), List.of(0, 1));

        assertThat(blockers).hasSize(2);
        assertThat(blockers).allMatch(b -> b[1] == 0);
    }

    @Test
    @DisplayName("Exhaustive: regular menace never returned with exactly one blocker")
    void exhaustiveMenaceNeverOneBlocker() {
        // Regular (non-lure, non-must-block) menace attacker. The enumeration must
        // never return a 1-blocker state — that would be an illegal declaration.
        Permanent menaceCreature = new Permanent(new GrizzlyBears());
        menaceCreature.getCard().setKeywords(EnumSet.of(Keyword.MENACE));
        menaceCreature.setSummoningSick(false);
        menaceCreature.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(menaceCreature);

        // 3 candidates — enumeration considers {0, 1, 2, 3} counts; 1 must be rejected.
        for (int i = 0; i < 3; i++) {
            Permanent aiBears = new Permanent(new GrizzlyBears());
            aiBears.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(aiBears);
        }

        List<int[]> blockers = simulator.findBestBlockersExhaustive(
                gd, player1.getId(), List.of(0), List.of(0, 1, 2));

        long menaceBlockerCount = blockers.stream().filter(b -> b[1] == 0).count();
        assertThat(menaceBlockerCount).isNotEqualTo(1);
    }
}
