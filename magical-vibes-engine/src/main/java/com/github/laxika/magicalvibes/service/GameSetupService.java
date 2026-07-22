package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.cards.PrebuiltDeck;
import com.github.laxika.magicalvibes.cards.RandomDeckGenerator;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Protocol-agnostic game bootstrap: registering a new game, seating players, and running the
 * opening sequence (deck shuffle, opening hands, mulligan phase, coin toss). This is engine-owned
 * game state logic — no lobby DTOs, connections, or wire messages. The backend {@code LobbyService}
 * wraps these calls to produce its lobby views; the AI and test harness call them directly.
 *
 * <p>Custom (user-built) decks are resolved through the optional {@link CustomDeckSource}; when none
 * is present only {@link PrebuiltDeck}s are available, which is all the AI and tests need.
 */
@Slf4j
@Service
public class GameSetupService {

    private final Random random = new Random();

    private final GameRegistry gameRegistry;
    private final ObjectProvider<CustomDeckSource> customDeckSourceProvider;

    public GameSetupService(GameRegistry gameRegistry,
                            ObjectProvider<CustomDeckSource> customDeckSourceProvider) {
        this.gameRegistry = gameRegistry;
        this.customDeckSourceProvider = customDeckSourceProvider;
    }

    /**
     * Creates and registers a new game seated with its creator. Returns the registered
     * {@link GameData} so callers can build their own presentation/DTOs from it.
     */
    public GameData createGame(String gameName, Player player, String deckId) {
        return createGame(gameName, player, deckId, false, null);
    }

    public GameData createGame(String gameName, Player player, String deckId, boolean allRandom) {
        return createGame(gameName, player, deckId, allRandom, null);
    }

    /**
     * Creates and registers a new game. When {@code allRandom} is set, the game runs in the
     * "All Random" mode: every player's deck choice is ignored and replaced with a freshly
     * generated {@link RandomDeckGenerator} deck. {@code randomSetCode} restricts the random decks
     * to a single set (by set code); {@code null} means all sets. It is only consulted when
     * {@code allRandom} is set.
     */
    public GameData createGame(String gameName, Player player, String deckId, boolean allRandom, String randomSetCode) {
        UUID gameId = UUID.randomUUID();

        if (allRandom) {
            deckId = RandomDeckGenerator.RANDOM_DECK_ID;
        }

        GameData gameData = new GameData(gameId, gameName, player.getId(), player.getUsername());
        gameData.allRandom = allRandom;
        gameData.randomSetCode = allRandom ? randomSetCode : null;
        gameData.playerIds.add(player.getId());
        gameData.orderedPlayerIds.add(player.getId());
        gameData.playerNames.add(player.getUsername());
        gameData.playerIdToName.put(player.getId(), player.getUsername());
        gameData.playerDeckChoices.put(player.getId(), deckId);
        gameRegistry.register(gameData);

        log.info("Game created: id={}, name='{}', creator={}", gameId, gameName, player.getUsername());
        return gameData;
    }

    /**
     * Seats a player into an existing game. Once the game has two players, the opening sequence
     * runs and the game advances to the mulligan phase.
     */
    public void joinGame(GameData gameData, Player player, String deckId) {
        synchronized (gameData) {
            if (gameData.status != GameStatus.WAITING) {
                throw new IllegalStateException("Game is not accepting players");
            }

            if (gameData.playerIds.contains(player.getId())) {
                throw new IllegalStateException("You are already in this game");
            }

            if (gameData.allRandom) {
                deckId = RandomDeckGenerator.RANDOM_DECK_ID;
            }

            gameData.playerIds.add(player.getId());
            gameData.orderedPlayerIds.add(player.getId());
            gameData.playerNames.add(player.getUsername());
            gameData.playerIdToName.put(player.getId(), player.getUsername());
            gameData.playerDeckChoices.put(player.getId(), deckId);

            if (gameData.playerIds.size() >= 2) {
                initializeGame(gameData);
            }

            log.info("User {} joined game {}, status={}", player.getUsername(), gameData.id, gameData.status);
        }
    }

    private void initializeGame(GameData gameData) {
        for (UUID playerId : gameData.playerIds) {
            String deckId = gameData.playerDeckChoices.get(playerId);
            List<Card> deck = resolveDeck(deckId, gameData.randomSetCode);

            // Stamp card ownership: each card is owned by the player whose deck it started in.
            // Preserved across zone changes; used to evaluate "a spell you don't own".
            // Then freeze: from here on the Card objects are shared with AI simulation copies
            // and must never be mutated (runtime state lives on Permanent/StackEntry/GameData).
            for (Card card : deck) {
                card.setOwnerId(playerId);
                card.freeze();
            }

            Collections.shuffle(deck, random);
            gameData.playerDecks.put(playerId, deck);
            gameData.mulliganCounts.put(playerId, 0);
            gameData.playerBattlefields.put(playerId, gameData.newBattlefieldList());
            gameData.playerGraveyards.put(playerId, new ArrayList<>());
            gameData.playerCommandZones.put(playerId, new ArrayList<>());
            gameData.playerManaPools.put(playerId, new ManaPool());
            gameData.playerLifeTotals.put(playerId, 20);

            List<Card> hand = new ArrayList<>(deck.subList(0, 7));
            deck.subList(0, 7).clear();
            gameData.playerHands.put(playerId, hand);

            Set<TurnStep> defaultStops = ConcurrentHashMap.newKeySet();
            defaultStops.add(TurnStep.PRECOMBAT_MAIN);
            defaultStops.add(TurnStep.POSTCOMBAT_MAIN);
            gameData.playerAutoStopSteps.put(playerId, defaultStops);
        }

        gameData.status = GameStatus.MULLIGAN;

        gameData.gameLog.add(GameLogEntry.text("Game started!"));
        for (UUID playerId : gameData.orderedPlayerIds) {
            String deckIdForLog = gameData.playerDeckChoices.get(playerId);
            String deckName;
            if (RandomDeckGenerator.RANDOM_DECK_ID.equals(deckIdForLog)) {
                deckName = "a randomly generated deck";
            } else if (isCustomDeck(deckIdForLog)) {
                deckName = "a custom deck";
            } else {
                deckName = PrebuiltDeck.findById(deckIdForLog).getDisplayName();
            }
            String playerName = gameData.playerIdToName.get(playerId);
            gameData.gameLog.add(GameLogEntry.text(playerName + " is playing with " + deckName + "."));
        }

        List<UUID> ids = new ArrayList<>(gameData.orderedPlayerIds);
        UUID startingPlayerId = ids.get(random.nextInt(ids.size()));
        String startingPlayerName = gameData.playerIdToName.get(startingPlayerId);
        gameData.startingPlayerId = startingPlayerId;

        gameData.gameLog.add(GameLogEntry.text(startingPlayerName + " wins the coin toss and goes first!"));
        gameData.gameLog.add(GameLogEntry.text("Mulligan phase — decide to keep or mulligan."));

        log.info("Game {} - Mulligan phase begins. Starting player: {}", gameData.id, startingPlayerName);
    }

    private boolean isCustomDeck(String deckId) {
        CustomDeckSource source = customDeckSourceProvider.getIfAvailable();
        return source != null && source.isCustomDeck(deckId);
    }

    private List<Card> resolveDeck(String deckId, String randomSetCode) {
        if (RandomDeckGenerator.RANDOM_DECK_ID.equals(deckId)) {
            return RandomDeckGenerator.generate(random, randomSetCode).cards();
        }
        CustomDeckSource source = customDeckSourceProvider.getIfAvailable();
        if (source != null && source.isCustomDeck(deckId)) {
            return source.buildCustomDeck(deckId);
        }
        return PrebuiltDeck.findById(deckId).buildDeck();
    }
}
