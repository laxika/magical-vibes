package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.ai.simulation.GameSimulator;
import com.github.laxika.magicalvibes.ai.simulation.MCTSEngine;
import com.github.laxika.magicalvibes.ai.simulation.SimulationAction;
import com.github.laxika.magicalvibes.cards.e.Eviscerate;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Micro-benchmark for {@link MCTSEngine#search} throughput and latency on a realistic
 * mid-game state (multi-creature boards, an anthem, five root actions, stocked libraries).
 *
 * <p>Measures two things:
 * <ul>
 *   <li><b>Fresh-tree throughput</b> — iterations completed inside the production time budget
 *       when the search starts from an empty tree ({@code clearCache()} between rounds).</li>
 *   <li><b>Warm-cache latency</b> — wall time of repeat searches at the same decision point,
 *       where the warm-start tree is already converged (the early-stopping best case).</li>
 * </ul>
 *
 * <p>Disabled by default; run with {@code -DmctsBench=true}:
 * <pre>./gradlew :magical-vibes-ai:test --tests "*.MCTSBenchmarkTest" -DmctsBench=true</pre>
 *
 * <p>The test JVM runs with {@code -XX:TieredStopAtLevel=1}, so absolute numbers are
 * pessimistic vs production — only before/after ratios on the same machine matter.
 */
@Tag("scryfall")
@EnabledIfSystemProperty(named = "mctsBench", matches = "true")
class MCTSBenchmarkTest {

    private static final long TIME_BUDGET_MS = MCTSEngine.DEFAULT_TIME_BUDGET_MS;
    private static final int ITERATION_BUDGET = 50000; // mirrors HardAiDecisionEngine.MCTS_BUDGET
    private static final int WARMUP_ROUNDS = 2;
    private static final int FRESH_ROUNDS = 5;
    private static final int REPEAT_ROUNDS = 4;

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;
    private GameSimulator simulator;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
        simulator = GameSimulator.forQueryService(harness.getGameQueryService());
        buildMidGameState();
    }

    /**
     * Mid-game snapshot: the AI is the active player in its precombat main with ten untapped
     * lands, two combat-ready creatures plus an anthem, and a hand producing five root
     * actions (Serra Angel, Hill Giant, Grizzly Bears, Eviscerate targeting a blocker, pass).
     * The opponent has a three-creature board and a stocked hand/library so determinization
     * and the greedy opponent rollout policy both have material to work with.
     */
    private void buildMidGameState() {
        // AI battlefield: 2 creatures + anthem + 10 untapped lands
        awake(harness.addToBattlefieldAndReturn(player1, new HillGiant()));
        awake(harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()));
        harness.addToBattlefield(player1, new GloriousAnthem());
        for (int i = 0; i < 3; i++) harness.addToBattlefield(player1, new Plains());
        for (int i = 0; i < 3; i++) harness.addToBattlefield(player1, new Forest());
        for (int i = 0; i < 2; i++) harness.addToBattlefield(player1, new Swamp());
        for (int i = 0; i < 2; i++) harness.addToBattlefield(player1, new Mountain());

        // Opponent battlefield: 3 creatures + lands
        awake(harness.addToBattlefieldAndReturn(player2, new HillGiant()));
        awake(harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()));
        awake(harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()));
        for (int i = 0; i < 4; i++) harness.addToBattlefield(player2, new Mountain());
        for (int i = 0; i < 2; i++) harness.addToBattlefield(player2, new Forest());

        // Hands: 5 root actions for the AI; opponent material for the determinizer
        harness.setHand(player1, List.of(
                new SerraAngel(), new HillGiant(), new GrizzlyBears(), new Eviscerate()));
        harness.setHand(player2, List.of(
                new HillGiant(), new GrizzlyBears(), new Mountain(), new Forest()));

        harness.setLibrary(player1, buildLibrary());
        harness.setLibrary(player2, buildLibrary());

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gd.stack.clear();
    }

    private static void awake(Permanent permanent) {
        permanent.setSummoningSick(false);
    }

    private List<Card> buildLibrary() {
        List<Card> library = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            library.add(new GrizzlyBears());
            library.add(new HillGiant());
            library.add(new Forest());
            library.add(new Mountain());
        }
        return library;
    }

    @Test
    void benchmarkSearch() {
        List<SimulationAction> rootActions = simulator.getLegalActions(gd, player1.getId());
        MCTSEngine engine = new MCTSEngine(simulator);
        engine.setTimeBudgetMs(TIME_BUDGET_MS);

        for (int i = 0; i < WARMUP_ROUNDS; i++) {
            engine.clearCache();
            engine.search(gd, player1.getId(), ITERATION_BUDGET);
        }

        System.out.printf("=== MCTS benchmark: %d root actions, budget %d ms, auto parallelism %d ===%n",
                rootActions.size(), TIME_BUDGET_MS, MCTSEngine.autoParallelism());

        engine.setParallelism(1);
        runFreshRounds(engine, "sequential");

        engine.setParallelism(MCTSEngine.autoParallelism());
        runFreshRounds(engine, "parallel");

        System.out.printf("--- Warm-cache repeat searches (%d rounds, parallel) ---%n",
                REPEAT_ROUNDS);
        int cacheHitsBefore = engine.getCacheHits();
        long totalRepeatElapsed = 0;
        for (int round = 1; round <= REPEAT_ROUNDS; round++) {
            SimulationAction action = engine.search(gd, player1.getId(), ITERATION_BUDGET);
            assertThat(action).isNotNull();
            totalRepeatElapsed += engine.getLastSearchElapsedMs();
            printRound("Repeat " + round, engine, action);
        }
        System.out.printf("REPEAT AVG: %.0f ms/search%n",
                totalRepeatElapsed / (double) REPEAT_ROUNDS);
        assertThat(engine.getCacheHits() - cacheHitsBefore).isEqualTo(REPEAT_ROUNDS);
    }

    private void runFreshRounds(MCTSEngine engine, String label) {
        System.out.printf("--- Fresh-tree searches (%d rounds, %s) ---%n", FRESH_ROUNDS, label);
        long totalIterations = 0;
        long totalElapsed = 0;
        for (int round = 1; round <= FRESH_ROUNDS; round++) {
            engine.clearCache();
            SimulationAction action = engine.search(gd, player1.getId(), ITERATION_BUDGET);
            assertThat(action).isNotNull();
            totalIterations += engine.getLastSearchIterations();
            totalElapsed += engine.getLastSearchElapsedMs();
            printRound("Round " + round, engine, action);
        }
        System.out.printf("FRESH %s AVG: %.1f iterations/search, %.1f iterations/s%n",
                label.toUpperCase(), totalIterations / (double) FRESH_ROUNDS,
                totalIterations * 1000.0 / Math.max(1, totalElapsed));
    }

    private void printRound(String label, MCTSEngine engine, SimulationAction action) {
        long elapsed = Math.max(1, engine.getLastSearchElapsedMs());
        System.out.printf("%s: %d iterations in %d ms (%.1f iter/s, %d failed)%s -> %s%n",
                label, engine.getLastSearchIterations(), engine.getLastSearchElapsedMs(),
                engine.getLastSearchIterations() * 1000.0 / elapsed,
                engine.getLastSearchFailures(),
                engine.isLastSearchEarlyStopped() ? " [early-stop]" : "", action);
    }
}
