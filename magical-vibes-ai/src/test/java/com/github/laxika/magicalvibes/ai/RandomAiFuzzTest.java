package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.cards.RandomDeckGenerator;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.GameRegistry;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import com.github.laxika.magicalvibes.websocket.WebSocketSessionManager;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.fail;

/**
 * Fuzz test that pits two Random AI players against each other with randomly built
 * 1–3-color decks (up to 4 copies per card). Unlike {@link AiVsAiStressTest} which
 * uses the smart Hard AI, this test uses purely random decision-making to exercise
 * far more edge cases — unusual spell timing, bizarre combat assignments, random
 * targets, random ability activations, occasional mulligans, etc.
 *
 * <p>Games are not reproducible from a seed: the engine draws from unseeded RNG
 * (shuffles, coin flips) and the two AI connections race on their own executors,
 * so replaying identical decisions is impossible anyway. Diagnose failures from
 * the captured stack traces and the game-state dump instead.</p>
 *
 * <p>Game-state invariants live in {@link FuzzInvariants}; a batch-wide coverage
 * report ({@link FuzzTelemetry}) is printed at the end of the run, even when a game
 * fails.</p>
 *
 * <p>Disabled by default; enable manually to run.</p>
 */
@Tag("scryfall")
@EnabledIfSystemProperty(named = "runCardFuzz", matches = "true")
class RandomAiFuzzTest {

    private static final int DEFAULT_GAME_COUNT = 50;
    private static final int MAX_SAME_STATE_COUNT = 30;
    private static final int MAX_TURNS = 250;
    private static final long POLL_INTERVAL_MS = 200;
    private static final long MAX_GAME_DURATION_MS = 300_000;
    private static final long AI_DECISION_DELAY_MS = 0;

    @Test
    void fuzzTestRandomAi() throws Exception {
        int gameCount = Integer.getInteger("fuzzGames", DEFAULT_GAME_COUNT);

        FuzzTelemetry telemetry = new FuzzTelemetry();
        try {
            int passed = 0;
            for (int game = 1; game <= gameCount; game++) {
                System.out.printf("=== Game #%d/%d ===%n", game, gameCount);
                long start = System.currentTimeMillis();
                runOneGame(game, telemetry);
                telemetry.recordGameCompleted();
                long elapsed = System.currentTimeMillis() - start;
                System.out.printf("=== Game #%d completed in %d ms ===%n%n", game, elapsed);
                passed++;
            }
            System.out.printf("All %d fuzz games passed.%n", passed);
        } finally {
            telemetry.printReport();
        }
    }

    // ------------------------------------------------------------------
    // Game lifecycle
    // ------------------------------------------------------------------

    private void runOneGame(int gameNumber, FuzzTelemetry telemetry) throws Exception {
        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            runOneGameInternal(gameNumber, new Random(), watcher, telemetry);
        } finally {
            watcher.uninstall();
        }
    }

    private void runOneGameInternal(int gameNumber, Random rng, FuzzLogWatcher watcher,
                                    FuzzTelemetry telemetry) throws Exception {
        // 1. Bootstrap the full service graph via the test harness
        GameTestHarness harness = new GameTestHarness();

        WebSocketSessionManager sessionManager = harness.getSessionManager();
        GameService gameService = harness.getGameService();
        GameRegistry gameRegistry = harness.getGameRegistry();
        GameQueryService gqs = harness.getGameQueryService();
        GameData gd = harness.getGameData();
        Player player1 = harness.getPlayer1();
        Player player2 = harness.getPlayer2();

        // 2-3. Generate random decks (colors + cards) and assign them
        RandomDeckGenerator randomDeckGenerator = GameTestHarness.randomDeckGenerator();
        RandomDeckGenerator.GeneratedDeck gen1 = randomDeckGenerator.generate(rng);
        RandomDeckGenerator.GeneratedDeck gen2 = randomDeckGenerator.generate(rng);

        System.out.printf("  Player 1: %s  |  Player 2: %s%n", gen1.colors(), gen2.colors());

        List<Card> deck1 = gen1.cards();
        List<Card> deck2 = gen2.cards();
        Collections.shuffle(deck1, rng);
        Collections.shuffle(deck2, rng);
        assignDeck(gd, player1.getId(), deck1);
        assignDeck(gd, player2.getId(), deck2);
        telemetry.recordDeckCards(deck1);
        telemetry.recordDeckCards(deck2);

        // Snapshot the identity of every card in the game for the conservation
        // invariant: none of these may ever vanish or appear in two zones at once.
        Map<UUID, String> initialCardNames = new HashMap<>();
        for (Card c : deck1) {
            initialCardNames.put(c.getId(), c.getName());
        }
        for (Card c : deck2) {
            initialCardNames.put(c.getId(), c.getName());
        }
        FuzzInvariants invariants = new FuzzInvariants(gqs, initialCardNames);

        // 4. Remove human transport connections; Random AI observes canonical facts directly.
        FakeConnection fakeConn1 = harness.getConn1();
        FakeConnection fakeConn2 = harness.getConn2();
        sessionManager.unregisterSession(fakeConn1.getId());
        sessionManager.unregisterSession(fakeConn2.getId());


        // Each engine gets its own Random derived from the master seed for thread safety
        RandomAiDecisionEngine engine1 = new RandomAiDecisionEngine(
                gd.id, player1, gameRegistry, gameService, gqs,
                harness.getBlockLegalityService(), harness.getCombatAttackService(), harness.getGameActionAvailabilityService(), harness.getCastingCostService(), harness.getCastingPermissionService(),
                harness.getTargetValidationService(), harness.getTargetLegalityService(), new Random(rng.nextLong()), telemetry);
        RandomAiDecisionEngine engine2 = new RandomAiDecisionEngine(
                gd.id, player2, gameRegistry, gameService, gqs,
                harness.getBlockLegalityService(), harness.getCombatAttackService(), harness.getGameActionAvailabilityService(), harness.getCastingCostService(), harness.getCastingPermissionService(),
                harness.getTargetValidationService(), harness.getTargetLegalityService(), new Random(rng.nextLong()), telemetry);

        AiDecisionScheduler aiConn1 = new AiDecisionScheduler("ai-fuzz-1", engine1, AI_DECISION_DELAY_MS);
        AiDecisionScheduler aiConn2 = new AiDecisionScheduler("ai-fuzz-2", engine2, AI_DECISION_DELAY_MS);

        gd.aiPlayerIds.addAll(List.of(player1.getId(), player2.getId()));
        AiDecisionEventSubscriber aiEvents = new AiDecisionEventSubscriber();
        aiEvents.register(gd.id, player1.getId(), aiConn1);
        aiEvents.register(gd.id, player2.getId(), aiConn2);
        AutoCloseable eventSubscription = harness.subscribeToGameEvents(aiEvents);

        // 5. Replay the authoritative pending mulligan facts now that the AI subscriber is
        // attached. Each engine randomly keeps or mulligans, exercising the London mulligan
        // and bottoming paths; the game transitions to RUNNING once both keep.
        harness.replayPendingMulliganDecisions();

        // 6. Poll until the game finishes (or gets stuck)
        String lastFingerprint = "";
        int sameCount = 0;
        long startTime = System.currentTimeMillis();

        while (gd.status != GameStatus.FINISHED) {
            Thread.sleep(POLL_INTERVAL_MS);

            List<String> logFailures = watcher.drainFailures();
            if (!logFailures.isEmpty()) {
                failGame("failure captured in logs:\n" + String.join("\n", logFailures),
                        gameNumber, gd, player1, player2, aiConn1, aiConn2);
            }

            String invariantViolation = invariants.check(gd);
            if (invariantViolation != null) {
                failGame(invariantViolation, gameNumber, gd, player1, player2, aiConn1, aiConn2);
            }

            if (System.currentTimeMillis() - startTime > MAX_GAME_DURATION_MS) {
                failGame("timed out after " + (MAX_GAME_DURATION_MS / 1000) + "s",
                        gameNumber, gd, player1, player2, aiConn1, aiConn2);
            }

            if (gd.turnNumber > MAX_TURNS) {
                System.out.printf("Game #%d declared a draw at turn %d (exceeded %d-turn cap)%n",
                        gameNumber, gd.turnNumber, MAX_TURNS);
                break;
            }

            String fingerprint = computeFingerprint(gd, player1.getId(), player2.getId());
            if (fingerprint.equals(lastFingerprint)) {
                sameCount++;

                if (sameCount >= MAX_SAME_STATE_COUNT) {
                    failGame("stuck — same state observed " + MAX_SAME_STATE_COUNT
                            + " consecutive times:\n" + fingerprint,
                            gameNumber, gd, player1, player2, aiConn1, aiConn2);
                }
            } else {
                sameCount = 0;
                lastFingerprint = fingerprint;
            }
        }

        // 7. Clean up executor threads
        aiConn1.close();
        aiConn2.close();
        eventSubscription.close();

        // Catch failures logged between the last poll and game end (e.g. a crash
        // during the final combat that also happened to end the game).
        List<String> trailingFailures = watcher.drainFailures();
        if (!trailingFailures.isEmpty()) {
            fail("Game #" + gameNumber + " finished, but failures were captured in logs:\n"
                    + String.join("\n", trailingFailures));
        }
    }

    private void failGame(String reason, int gameNumber, GameData gd, Player p1, Player p2,
                          AiDecisionScheduler conn1, AiDecisionScheduler conn2) {
        dumpGameState(gameNumber, gd, p1, p2, conn1, conn2);
        conn1.close();
        conn2.close();
        fail("Game #" + gameNumber + " " + reason);
    }

    // ------------------------------------------------------------------
    // Deck assignment
    // ------------------------------------------------------------------

    private void assignDeck(GameData gd, UUID playerId, List<Card> fullDeck) {
        List<Card> hand = new ArrayList<>(fullDeck.subList(0, 7));
        List<Card> library = new ArrayList<>(fullDeck.subList(7, fullDeck.size()));
        gd.playerHands.put(playerId, hand);
        gd.playerDecks.put(playerId, library);
    }

    // ------------------------------------------------------------------
    // Stuck-state detection helpers
    // ------------------------------------------------------------------

    private String computeFingerprint(GameData gd, UUID p1, UUID p2) {
        synchronized (gd) {
            return gd.turnNumber
                    + ":" + gd.currentStep
                    + ":" + gd.activePlayerId
                    + ":" + gd.stack.size()
                    + ":" + gd.priorityPassedBy
                    + ":" + gd.playerLifeTotals.getOrDefault(p1, 0)
                    + ":" + gd.playerLifeTotals.getOrDefault(p2, 0)
                    + ":" + gd.playerHands.getOrDefault(p1, List.of()).size()
                    + ":" + gd.playerHands.getOrDefault(p2, List.of()).size()
                    + ":" + gd.playerBattlefields.getOrDefault(p1, List.of()).size()
                    + ":" + gd.playerBattlefields.getOrDefault(p2, List.of()).size()
                    + ":" + gd.playerGraveyards.getOrDefault(p1, List.of()).size()
                    + ":" + gd.playerGraveyards.getOrDefault(p2, List.of()).size()
                    + ":" + gd.interaction.isAwaitingInput();
        }
    }

    private void dumpGameState(int gameNumber, GameData gd, Player p1, Player p2,
                               AiDecisionScheduler conn1, AiDecisionScheduler conn2) {
        synchronized (gd) {
            System.err.println("=== STUCK GAME STATE — Game #" + gameNumber + " ===");
            System.err.println("Turn:            " + gd.turnNumber);
            System.err.println("Step:            " + gd.currentStep);
            System.err.println("Active player:   " + gd.activePlayerId);
            System.err.println("Stack size:      " + gd.stack.size());
            System.err.println("Priority passed: " + gd.priorityPassedBy);
            System.err.println("Awaiting input:  " + gd.interaction.isAwaitingInput());
            System.err.println("Interaction:     " + gd.interaction.activeInteraction());
            System.err.println("AI 1 executor:   " + conn1.diagnosticSummary());
            System.err.println("AI 2 executor:   " + conn2.diagnosticSummary());
            System.err.printf("P1 (%s): life=%d hand=%d battlefield=%d graveyard=%d%n",
                    p1.getUsername(),
                    gd.playerLifeTotals.getOrDefault(p1.getId(), 0),
                    gd.playerHands.getOrDefault(p1.getId(), List.of()).size(),
                    gd.playerBattlefields.getOrDefault(p1.getId(), List.of()).size(),
                    gd.playerGraveyards.getOrDefault(p1.getId(), List.of()).size());
            System.err.printf("P2 (%s): life=%d hand=%d battlefield=%d graveyard=%d%n",
                    p2.getUsername(),
                    gd.playerLifeTotals.getOrDefault(p2.getId(), 0),
                    gd.playerHands.getOrDefault(p2.getId(), List.of()).size(),
                    gd.playerBattlefields.getOrDefault(p2.getId(), List.of()).size(),
                    gd.playerGraveyards.getOrDefault(p2.getId(), List.of()).size());

            if (!gd.stack.isEmpty()) {
                System.err.println("Stack contents:");
                gd.stack.forEach(entry ->
                        System.err.println("  - " + entry.getCard().getName()));
            }

            List<GameLogEntry> log = gd.gameLog;
            int start = Math.max(0, log.size() - 30);
            System.err.println("Last " + (log.size() - start) + " log entries:");
            for (int i = start; i < log.size(); i++) {
                System.err.println("  " + log.get(i).plainText());
            }
            System.err.println("=== END DUMP ===");
        }
    }

}
