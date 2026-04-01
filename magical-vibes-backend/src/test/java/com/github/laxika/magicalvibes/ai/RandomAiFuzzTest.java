package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.cards.CardPrinting;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.config.JacksonConfig;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.MessageHandler;
import com.github.laxika.magicalvibes.service.GameRegistry;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import com.github.laxika.magicalvibes.websocket.WebSocketSessionManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.fail;

/**
 * Fuzz test that pits two Random AI players against each other with randomly built
 * 2-color decks. Unlike {@link AiVsAiStressTest} which uses the smart Hard AI, this
 * test uses purely random decision-making to exercise far more edge cases — unusual
 * spell timing, bizarre combat assignments, random targets, etc.
 *
 * <p>Each game prints its random seed so failures can be reproduced by hardcoding
 * the seed in code or via {@code -DfuzzSeed=12345} system property.</p>
 *
 * <p>Disabled by default; enable manually to run.</p>
 */
@EnabledIfSystemProperty(named = "runCardFuzz", matches = "true")
class RandomAiFuzzTest {

    private static final int DEFAULT_GAME_COUNT = 50;
    private static final int DECK_SIZE = 40;
    private static final int LAND_COUNT = 18;
    private static final int SPELL_COUNT = DECK_SIZE - LAND_COUNT;
    private static final int MAX_SAME_STATE_COUNT = 30;
    private static final long POLL_INTERVAL_MS = 200;
    private static final long MAX_GAME_DURATION_MS = 300_000;
    private static final long AI_DECISION_DELAY_MS = 10;

    private static final Map<CardColor, CardPrinting> BASIC_LAND_PRINTINGS = new EnumMap<>(CardColor.class);
    private static List<CardPrinting> allNonLandPrintings;

    @Test
    void fuzzTestRandomAi() throws Exception {
        int gameCount = Integer.getInteger("fuzzGames", DEFAULT_GAME_COUNT);
        Long fixedSeed = Long.getLong("fuzzSeed");

        int passed = 0;
        for (int game = 1; game <= gameCount; game++) {
            long seed = fixedSeed != null ? fixedSeed : System.nanoTime();
            System.out.printf("=== Game #%d/%d  seed=%d ===%n", game, gameCount, seed);
            long start = System.currentTimeMillis();
            runOneGame(game, new Random(seed));
            long elapsed = System.currentTimeMillis() - start;
            System.out.printf("=== Game #%d completed in %d ms ===%n%n", game, elapsed);
            passed++;
        }
        System.out.printf("All %d fuzz games passed.%n", passed);
    }

    // ------------------------------------------------------------------
    // Game lifecycle
    // ------------------------------------------------------------------

    private void runOneGame(int gameNumber, Random rng) throws Exception {
        // 1. Bootstrap the full service graph via the test harness
        GameTestHarness harness = new GameTestHarness();
        initializeCardPool();

        WebSocketSessionManager sessionManager = harness.getSessionManager();
        MessageHandler messageHandler = harness.getMessageHandler();
        GameRegistry gameRegistry = harness.getGameRegistry();
        GameQueryService gqs = harness.getGameQueryService();
        GameData gd = harness.getGameData();
        Player player1 = harness.getPlayer1();
        Player player2 = harness.getPlayer2();

        // 2. Pick random 2-color combinations (independently for each player)
        CardColor[] p1Colors = pickTwoColors(rng);
        CardColor[] p2Colors = pickTwoColors(rng);

        System.out.printf("  Player 1: %s/%s  |  Player 2: %s/%s%n",
                p1Colors[0], p1Colors[1], p2Colors[0], p2Colors[1]);

        // 3. Build random decks and assign them
        List<Card> deck1 = buildRandomDeck(p1Colors[0], p1Colors[1], rng);
        List<Card> deck2 = buildRandomDeck(p2Colors[0], p2Colors[1], rng);
        Collections.shuffle(deck1, rng);
        Collections.shuffle(deck2, rng);
        assignDeck(gd, player1.getId(), deck1);
        assignDeck(gd, player2.getId(), deck2);

        // 4. Replace the harness's FakeConnections with Random AI connections
        FakeConnection fakeConn1 = harness.getConn1();
        FakeConnection fakeConn2 = harness.getConn2();
        sessionManager.unregisterSession(fakeConn1.getId());
        sessionManager.unregisterSession(fakeConn2.getId());

        ObjectMapper objectMapper = new JacksonConfig().objectMapper();

        // Each engine gets its own Random derived from the master seed for thread safety
        RandomAiDecisionEngine engine1 = new RandomAiDecisionEngine(
                gd.id, player1, gameRegistry, messageHandler, gqs,
                harness.getCombatAttackService(), harness.getGameBroadcastService(),
                harness.getTargetValidationService(), harness.getTargetLegalityService(), new Random(rng.nextLong()));
        RandomAiDecisionEngine engine2 = new RandomAiDecisionEngine(
                gd.id, player2, gameRegistry, messageHandler, gqs,
                harness.getCombatAttackService(), harness.getGameBroadcastService(),
                harness.getTargetValidationService(), harness.getTargetLegalityService(), new Random(rng.nextLong()));

        AiConnection aiConn1 = new AiConnection("ai-fuzz-1", engine1, objectMapper, AI_DECISION_DELAY_MS);
        AiConnection aiConn2 = new AiConnection("ai-fuzz-2", engine2, objectMapper, AI_DECISION_DELAY_MS);
        engine1.setSelfConnection(aiConn1);
        engine2.setSelfConnection(aiConn2);

        sessionManager.registerPlayer(aiConn1, player1.getId(), "Random AI 1");
        sessionManager.registerPlayer(aiConn2, player2.getId(), "Random AI 2");
        sessionManager.setInGame("ai-fuzz-1");
        sessionManager.setInGame("ai-fuzz-2");

        // 5. Both AIs keep their opening hand — transitions the game to RUNNING
        harness.getGameService().keepHand(gd, player1);
        harness.getGameService().keepHand(gd, player2);

        // 6. Poll until the game finishes (or gets stuck)
        String lastFingerprint = "";
        int sameCount = 0;
        long startTime = System.currentTimeMillis();

        while (gd.status != GameStatus.FINISHED) {
            Thread.sleep(POLL_INTERVAL_MS);

            if (System.currentTimeMillis() - startTime > MAX_GAME_DURATION_MS) {
                dumpGameState(gameNumber, gd, player1, player2);
                aiConn1.close();
                aiConn2.close();
                fail("Game #" + gameNumber + " timed out after " + (MAX_GAME_DURATION_MS / 1000) + "s");
            }

            String fingerprint = computeFingerprint(gd, player1.getId(), player2.getId());
            if (fingerprint.equals(lastFingerprint)) {
                sameCount++;

                if (sameCount >= MAX_SAME_STATE_COUNT) {
                    dumpGameState(gameNumber, gd, player1, player2);
                    aiConn1.close();
                    aiConn2.close();
                    fail("Game #" + gameNumber + " stuck — same state observed "
                            + MAX_SAME_STATE_COUNT + " consecutive times:\n" + fingerprint);
                }
            } else {
                sameCount = 0;
                lastFingerprint = fingerprint;
            }
        }

        // 7. Clean up executor threads
        aiConn1.close();
        aiConn2.close();
    }

    // ------------------------------------------------------------------
    // Random deck construction (same as AiVsAiStressTest)
    // ------------------------------------------------------------------

    private CardColor[] pickTwoColors(Random rng) {
        CardColor[] all = CardColor.values();
        int i = rng.nextInt(all.length);
        int j;
        do {
            j = rng.nextInt(all.length);
        } while (j == i);
        return new CardColor[]{all[i], all[j]};
    }

    private List<Card> buildRandomDeck(CardColor c1, CardColor c2, Random rng) {
        Set<CardColor> deckColors = EnumSet.of(c1, c2);

        List<CardPrinting> playable = new ArrayList<>();
        for (CardPrinting printing : allNonLandPrintings) {
            Card sample = printing.createCard();
            if (deckColors.containsAll(sample.getColors())) {
                playable.add(printing);
            }
        }
        Collections.shuffle(playable, rng);

        List<Card> deck = new ArrayList<>();

        for (int i = 0; i < Math.min(SPELL_COUNT, playable.size()); i++) {
            deck.add(playable.get(i).createCard());
        }

        int landsPerColor = LAND_COUNT / 2;
        int extra = LAND_COUNT % 2;
        CardPrinting land1 = BASIC_LAND_PRINTINGS.get(c1);
        CardPrinting land2 = BASIC_LAND_PRINTINGS.get(c2);
        for (int i = 0; i < landsPerColor + extra; i++) {
            deck.add(land1.createCard());
        }
        for (int i = 0; i < landsPerColor; i++) {
            deck.add(land2.createCard());
        }

        return deck;
    }

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

    private void dumpGameState(int gameNumber, GameData gd, Player p1, Player p2) {
        synchronized (gd) {
            System.err.println("=== STUCK GAME STATE — Game #" + gameNumber + " ===");
            System.err.println("Turn:            " + gd.turnNumber);
            System.err.println("Step:            " + gd.currentStep);
            System.err.println("Active player:   " + gd.activePlayerId);
            System.err.println("Stack size:      " + gd.stack.size());
            System.err.println("Priority passed: " + gd.priorityPassedBy);
            System.err.println("Awaiting input:  " + gd.interaction.isAwaitingInput());
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

            List<String> log = gd.gameLog;
            int start = Math.max(0, log.size() - 30);
            System.err.println("Last " + (log.size() - start) + " log entries:");
            for (int i = start; i < log.size(); i++) {
                System.err.println("  " + log.get(i));
            }
            System.err.println("=== END DUMP ===");
        }
    }

    // ------------------------------------------------------------------
    // One-time card-pool initialization (reused across games)
    // ------------------------------------------------------------------

    private static synchronized void initializeCardPool() {
        if (allNonLandPrintings != null) {
            return;
        }

        BASIC_LAND_PRINTINGS.put(CardColor.WHITE, CardSet.SCARS_OF_MIRRODIN.findByCollectorNumber("230"));
        BASIC_LAND_PRINTINGS.put(CardColor.BLUE, CardSet.SCARS_OF_MIRRODIN.findByCollectorNumber("234"));
        BASIC_LAND_PRINTINGS.put(CardColor.BLACK, CardSet.SCARS_OF_MIRRODIN.findByCollectorNumber("238"));
        BASIC_LAND_PRINTINGS.put(CardColor.RED, CardSet.SCARS_OF_MIRRODIN.findByCollectorNumber("242"));
        BASIC_LAND_PRINTINGS.put(CardColor.GREEN, CardSet.SCARS_OF_MIRRODIN.findByCollectorNumber("246"));

        allNonLandPrintings = new ArrayList<>();
        for (CardSet set : CardSet.values()) {
            for (CardPrinting printing : set.getPrintings()) {
                Card sample = printing.createCard();
                if (!sample.hasType(CardType.LAND) && sample.getManaCost() != null) {
                    allNonLandPrintings.add(printing);
                }
            }
        }

        System.out.printf("Card pool initialized: %d non-land printings, %d basic-land printings%n",
                allNonLandPrintings.size(), BASIC_LAND_PRINTINGS.size());
    }
}
