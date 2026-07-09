package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.cards.CardPrinting;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.service.JacksonConfig;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.service.GameRegistry;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import com.github.laxika.magicalvibes.websocket.WebSocketSessionManager;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.fail;

/**
 * Fuzz test that pits two Random AI players against each other with randomly built
 * 1â€“3-color decks (up to 4 copies per card). Unlike {@link AiVsAiStressTest} which
 * uses the smart Hard AI, this test uses purely random decision-making to exercise
 * far more edge cases â€” unusual spell timing, bizarre combat assignments, random
 * targets, random ability activations, occasional mulligans, etc.
 *
 * <p>Each game prints its random seed so failures can be reproduced by hardcoding
 * the seed in code or via {@code -DfuzzSeed=12345} system property.</p>
 *
 * <p>Disabled by default; enable manually to run.</p>
 */
@Tag("scryfall")
@EnabledIfSystemProperty(named = "runCardFuzz", matches = "true")
class RandomAiFuzzTest {

    private static final int DEFAULT_GAME_COUNT = 50;
    private static final int DECK_SIZE = 40;
    private static final int LAND_COUNT = 18;
    private static final int SPELL_COUNT = DECK_SIZE - LAND_COUNT;
    private static final int MAX_SAME_STATE_COUNT = 30;
    private static final int MAX_TURNS = 250;
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
            runOneGame(game, seed);
            long elapsed = System.currentTimeMillis() - start;
            System.out.printf("=== Game #%d completed in %d ms ===%n%n", game, elapsed);
            passed++;
        }
        System.out.printf("All %d fuzz games passed.%n", passed);
    }

    // ------------------------------------------------------------------
    // Game lifecycle
    // ------------------------------------------------------------------

    private void runOneGame(int gameNumber, long seed) throws Exception {
        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            runOneGameInternal(gameNumber, seed, new Random(seed), watcher);
        } finally {
            watcher.uninstall();
        }
    }

    private void runOneGameInternal(int gameNumber, long seed, Random rng, FuzzLogWatcher watcher) throws Exception {
        // 1. Bootstrap the full service graph via the test harness
        GameTestHarness harness = new GameTestHarness();
        initializeCardPool();

        WebSocketSessionManager sessionManager = harness.getSessionManager();
        GameService gameService = harness.getGameService();
        GameRegistry gameRegistry = harness.getGameRegistry();
        GameQueryService gqs = harness.getGameQueryService();
        GameData gd = harness.getGameData();
        Player player1 = harness.getPlayer1();
        Player player2 = harness.getPlayer2();

        // 2. Pick random color combinations (independently for each player)
        Set<CardColor> p1Colors = pickDeckColors(rng);
        Set<CardColor> p2Colors = pickDeckColors(rng);

        System.out.printf("  Player 1: %s  |  Player 2: %s%n", p1Colors, p2Colors);

        // 3. Build random decks and assign them
        List<Card> deck1 = buildRandomDeck(p1Colors, rng);
        List<Card> deck2 = buildRandomDeck(p2Colors, rng);
        Collections.shuffle(deck1, rng);
        Collections.shuffle(deck2, rng);
        assignDeck(gd, player1.getId(), deck1);
        assignDeck(gd, player2.getId(), deck2);

        // Snapshot the identity of every card in the game for the conservation
        // invariant: none of these may ever vanish or appear in two zones at once.
        Map<UUID, String> initialCardNames = new HashMap<>();
        for (Card c : deck1) {
            initialCardNames.put(c.getId(), c.getName());
        }
        for (Card c : deck2) {
            initialCardNames.put(c.getId(), c.getName());
        }

        // 4. Replace the harness's FakeConnections with Random AI connections
        FakeConnection fakeConn1 = harness.getConn1();
        FakeConnection fakeConn2 = harness.getConn2();
        sessionManager.unregisterSession(fakeConn1.getId());
        sessionManager.unregisterSession(fakeConn2.getId());

        ObjectMapper objectMapper = new JacksonConfig().objectMapper();

        // Each engine gets its own Random derived from the master seed for thread safety
        RandomAiDecisionEngine engine1 = new RandomAiDecisionEngine(
                gd.id, player1, gameRegistry, gameService, gqs,
                harness.getCombatAttackService(), harness.getGameBroadcastService(), harness.getCastingCostService(), harness.getCastingPermissionService(),
                harness.getTargetValidationService(), harness.getTargetLegalityService(), new Random(rng.nextLong()));
        RandomAiDecisionEngine engine2 = new RandomAiDecisionEngine(
                gd.id, player2, gameRegistry, gameService, gqs,
                harness.getCombatAttackService(), harness.getGameBroadcastService(), harness.getCastingCostService(), harness.getCastingPermissionService(),
                harness.getTargetValidationService(), harness.getTargetLegalityService(), new Random(rng.nextLong()));

        AiConnection aiConn1 = new AiConnection("ai-fuzz-1", engine1, objectMapper, AI_DECISION_DELAY_MS);
        AiConnection aiConn2 = new AiConnection("ai-fuzz-2", engine2, objectMapper, AI_DECISION_DELAY_MS);
        engine1.setSelfConnection(aiConn1);
        engine2.setSelfConnection(aiConn2);

        sessionManager.registerPlayer(aiConn1, player1.getId(), "Random AI 1");
        sessionManager.registerPlayer(aiConn2, player2.getId(), "Random AI 2");
        sessionManager.setInGame("ai-fuzz-1");
        sessionManager.setInGame("ai-fuzz-2");

        // 5. Kick off the mulligan phase for both AIs (mirrors AiPlayerService).
        // Each engine randomly keeps or mulligans, exercising the London mulligan
        // and bottoming paths; the game transitions to RUNNING once both keep.
        aiConn1.scheduleInitialAction(engine1::handleInitialMulligan);
        aiConn2.scheduleInitialAction(engine2::handleInitialMulligan);

        // 6. Poll until the game finishes (or gets stuck)
        String lastFingerprint = "";
        int sameCount = 0;
        long startTime = System.currentTimeMillis();
        TwoStrikeState strikes = new TwoStrikeState();

        while (gd.status != GameStatus.FINISHED) {
            Thread.sleep(POLL_INTERVAL_MS);

            List<String> logFailures = watcher.drainFailures();
            if (!logFailures.isEmpty()) {
                failGame("failure captured in logs:\n" + String.join("\n", logFailures),
                        gameNumber, seed, gd, player1, player2, aiConn1, aiConn2);
            }

            String invariantViolation = checkInvariants(gd, initialCardNames, strikes, gqs);
            if (invariantViolation != null) {
                failGame(invariantViolation, gameNumber, seed, gd, player1, player2, aiConn1, aiConn2);
            }

            if (System.currentTimeMillis() - startTime > MAX_GAME_DURATION_MS) {
                failGame("timed out after " + (MAX_GAME_DURATION_MS / 1000) + "s",
                        gameNumber, seed, gd, player1, player2, aiConn1, aiConn2);
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
                    failGame("stuck â€” same state observed " + MAX_SAME_STATE_COUNT
                            + " consecutive times:\n" + fingerprint,
                            gameNumber, seed, gd, player1, player2, aiConn1, aiConn2);
                }
            } else {
                sameCount = 0;
                lastFingerprint = fingerprint;
            }
        }

        // 7. Clean up executor threads
        aiConn1.close();
        aiConn2.close();

        // Catch failures logged between the last poll and game end (e.g. a crash
        // during the final combat that also happened to end the game).
        List<String> trailingFailures = watcher.drainFailures();
        if (!trailingFailures.isEmpty()) {
            fail("Game #" + gameNumber + " (seed=" + seed + ") finished, but failures were captured in logs:\n"
                    + String.join("\n", trailingFailures));
        }
    }

    private void failGame(String reason, int gameNumber, long seed, GameData gd, Player p1, Player p2,
                          AiConnection conn1, AiConnection conn2) {
        dumpGameState(gameNumber, gd, p1, p2);
        conn1.close();
        conn2.close();
        fail("Game #" + gameNumber + " (seed=" + seed + ") " + reason);
    }

    // ------------------------------------------------------------------
    // Game-state invariants
    // ------------------------------------------------------------------

    /**
     * Conservation and SBA violations must be observed on two consecutive polls
     * before failing: a poll can land between two engine steps of a multi-part
     * zone move, so a single observation may be a transient, not a bug.
     */
    private static final class TwoStrikeState {
        String lastConservationViolation;
        String lastSbaViolation;
    }

    /**
     * Returns a violation description, or {@code null} if all invariants hold.
     * Structural violations (duplicate/corrupt permanents) fail immediately;
     * conservation and SBA violations use the two-strike rule.
     */
    private String checkInvariants(GameData gd, Map<UUID, String> initialCardNames,
                                   TwoStrikeState strikes, GameQueryService gqs) {
        synchronized (gd) {
            Set<UUID> seenPermanentIds = new HashSet<>();
            for (UUID pid : gd.orderedPlayerIds) {
                for (Permanent p : gd.playerBattlefields.getOrDefault(pid, List.of())) {
                    if (p.getCard() == null) {
                        return "invariant violated: permanent " + p.getId() + " has a null card";
                    }
                    if (!seenPermanentIds.add(p.getId())) {
                        return "invariant violated: permanent " + p.getCard().getName()
                                + " (" + p.getId() + ") appears on multiple battlefields";
                    }
                }
            }

            // Skip zone-content checks while the engine is holding cards aside for a
            // pending choice (e.g. "look at the top N") â€” they are legitimately
            // outside every zone at that moment.
            if (gd.interaction.isAwaitingInput()) {
                return null;
            }

            String conservation = findConservationViolation(gd, initialCardNames);
            if (conservation != null && conservation.equals(strikes.lastConservationViolation)) {
                return conservation;
            }
            strikes.lastConservationViolation = conservation;

            if (gd.stack.isEmpty()) {
                String sba = findSbaViolation(gd, gqs);
                if (sba != null && sba.equals(strikes.lastSbaViolation)) {
                    return sba;
                }
                strikes.lastSbaViolation = sba;
            }
        }
        return null;
    }

    /**
     * Every card the game started with must appear exactly once across all zones:
     * libraries, hands, graveyards, battlefields, exile and spells on the stack.
     * Token and copy cards created mid-game are ignored (they may legitimately
     * cease to exist). Battlefields are counted via {@link Permanent#getOriginalCard()}
     * because that is the object the engine moves between zones (transformed
     * permanents carry a different face on {@code getCard()}).
     */
    private String findConservationViolation(GameData gd, Map<UUID, String> initialCardNames) {
        Map<UUID, Integer> counts = new HashMap<>();
        for (UUID pid : gd.orderedPlayerIds) {
            countCardIds(gd.playerDecks.get(pid), counts);
            countCardIds(gd.playerHands.get(pid), counts);
            countCardIds(gd.playerGraveyards.get(pid), counts);
            for (Permanent p : gd.playerBattlefields.getOrDefault(pid, List.of())) {
                if (p.getOriginalCard() != null) {
                    counts.merge(p.getOriginalCard().getId(), 1, Integer::sum);
                }
            }
        }
        for (ExiledCardEntry entry : gd.exiledCards) {
            counts.merge(entry.card().getId(), 1, Integer::sum);
        }
        for (StackEntry entry : gd.stack) {
            // Ability entries reference a source card that is already counted in its
            // own zone; copy entries were never part of the initial pool.
            if (entry.isCopy()
                    || entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                    || entry.getEntryType() == StackEntryType.ACTIVATED_ABILITY) {
                continue;
            }
            if (entry.getCard() != null) {
                counts.merge(entry.getCard().getId(), 1, Integer::sum);
            }
        }

        List<String> problems = new ArrayList<>();
        for (Map.Entry<UUID, String> expected : initialCardNames.entrySet()) {
            int count = counts.getOrDefault(expected.getKey(), 0);
            if (count != 1) {
                problems.add(expected.getValue() + " (" + expected.getKey() + ") found "
                        + count + " times across all zones");
            }
        }
        return problems.isEmpty() ? null
                : "card conservation violated: " + String.join("; ", problems);
    }

    private void countCardIds(List<Card> cards, Map<UUID, Integer> counts) {
        if (cards == null) {
            return;
        }
        for (Card c : cards) {
            counts.merge(c.getId(), 1, Integer::sum);
        }
    }

    /**
     * With an empty stack and no pending input, state-based actions must have
     * finished: no creature with toughness &le; 0 may still be on a battlefield.
     * (Toughness only â€” lethal-damage SBAs are excluded because indestructible
     * and regeneration make them unreliable to verify from outside the engine.)
     */
    private String findSbaViolation(GameData gd, GameQueryService gqs) {
        List<String> problems = new ArrayList<>();
        for (UUID pid : gd.orderedPlayerIds) {
            for (Permanent p : gd.playerBattlefields.getOrDefault(pid, List.of())) {
                if (gqs.isCreature(gd, p)) {
                    int toughness = gqs.getEffectiveToughness(gd, p);
                    if (toughness <= 0) {
                        problems.add(p.getCard().getName() + " (" + p.getId() + ") has toughness "
                                + toughness + " but survived state-based actions");
                    }
                }
            }
        }
        return problems.isEmpty() ? null : "SBA violation: " + String.join("; ", problems);
    }

    // ------------------------------------------------------------------
    // Random deck construction (same as AiVsAiStressTest)
    // ------------------------------------------------------------------

    private Set<CardColor> pickDeckColors(Random rng) {
        // 20% mono-color, 60% two-color, 20% three-color
        int roll = rng.nextInt(10);
        int colorCount = roll < 2 ? 1 : roll < 8 ? 2 : 3;
        List<CardColor> all = new ArrayList<>(List.of(CardColor.values()));
        Collections.shuffle(all, rng);
        return EnumSet.copyOf(all.subList(0, colorCount));
    }

    private List<Card> buildRandomDeck(Set<CardColor> deckColors, Random rng) {
        List<CardPrinting> playable = new ArrayList<>();
        for (CardPrinting printing : allNonLandPrintings) {
            Card sample = printing.createCard();
            if (deckColors.containsAll(sample.getColors())) {
                playable.add(printing);
            }
        }

        // Sample with replacement, up to 4 copies per printing, so same-card
        // interactions (multiple copies in play, in the graveyard, legend rule)
        // get exercised too.
        List<Card> deck = new ArrayList<>();
        Map<CardPrinting, Integer> copiesUsed = new HashMap<>();
        while (deck.size() < SPELL_COUNT && !playable.isEmpty()) {
            int idx = rng.nextInt(playable.size());
            CardPrinting printing = playable.get(idx);
            deck.add(printing.createCard());
            if (copiesUsed.merge(printing, 1, Integer::sum) >= 4) {
                playable.remove(idx);
            }
        }

        // Distribute lands as evenly as possible across the deck's colors
        List<CardColor> colors = new ArrayList<>(deckColors);
        for (int i = 0; i < LAND_COUNT; i++) {
            deck.add(BASIC_LAND_PRINTINGS.get(colors.get(i % colors.size())).createCard());
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
            System.err.println("=== STUCK GAME STATE â€” Game #" + gameNumber + " ===");
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

        BASIC_LAND_PRINTINGS.put(CardColor.WHITE, CardSet.SET_SOM.findByCollectorNumber("230"));
        BASIC_LAND_PRINTINGS.put(CardColor.BLUE, CardSet.SET_SOM.findByCollectorNumber("234"));
        BASIC_LAND_PRINTINGS.put(CardColor.BLACK, CardSet.SET_SOM.findByCollectorNumber("238"));
        BASIC_LAND_PRINTINGS.put(CardColor.RED, CardSet.SET_SOM.findByCollectorNumber("242"));
        BASIC_LAND_PRINTINGS.put(CardColor.GREEN, CardSet.SET_SOM.findByCollectorNumber("246"));

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
