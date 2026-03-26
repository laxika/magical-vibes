package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.cards.PrebuiltDeck;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.JoinGame;
import com.github.laxika.magicalvibes.networking.message.LobbyGame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class LobbyService {

    public record GameResult(JoinGame joinGame, LobbyGame lobbyGame) {}

    private final Random random = new Random();

    private final GameRegistry gameRegistry;
    private final GameBroadcastService gameBroadcastService;
    private final DeckService deckService;

    public GameResult createGame(String gameName, Player player, String deckId) {
        UUID gameId = UUID.randomUUID();

        GameData gameData = new GameData(gameId, gameName, player.getId(), player.getUsername());
        gameData.playerIds.add(player.getId());
        gameData.orderedPlayerIds.add(player.getId());
        gameData.playerNames.add(player.getUsername());
        gameData.playerIdToName.put(player.getId(), player.getUsername());
        gameData.playerDeckChoices.put(player.getId(), deckId);
        gameRegistry.register(gameData);

        log.info("Game created: id={}, name='{}', creator={}", gameId, gameName, player.getUsername());
        return new GameResult(gameBroadcastService.getJoinGame(gameData, null), toLobbyGame(gameData));
    }

    public List<LobbyGame> listRunningGames() {
        return gameRegistry.getRunningGames().stream()
                .map(this::toLobbyGame)
                .toList();
    }

    public LobbyGame joinGame(GameData gameData, Player player, String deckId) {
        synchronized (gameData) {
            if (gameData.status != GameStatus.WAITING) {
                throw new IllegalStateException("Game is not accepting players");
            }

            if (gameData.playerIds.contains(player.getId())) {
                throw new IllegalStateException("You are already in this game");
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
            return toLobbyGame(gameData);
        }
    }

    private void initializeGame(GameData gameData) {
        for (UUID playerId : gameData.playerIds) {
            String deckId = gameData.playerDeckChoices.get(playerId);
            List<Card> deck;
            if (deckService.isCustomDeck(deckId)) {
                deck = deckService.buildCustomDeck(deckId);
            } else {
                PrebuiltDeck prebuiltDeck = PrebuiltDeck.findById(deckId);
                deck = prebuiltDeck.buildDeck();
            }

            Collections.shuffle(deck, random);
            gameData.playerDecks.put(playerId, deck);
            gameData.mulliganCounts.put(playerId, 0);
            gameData.playerBattlefields.put(playerId, new ArrayList<>());
            gameData.playerGraveyards.put(playerId, new ArrayList<>());
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

        gameData.gameLog.add("Game started!");
        for (UUID playerId : gameData.orderedPlayerIds) {
            String deckIdForLog = gameData.playerDeckChoices.get(playerId);
            String deckName;
            if (deckService.isCustomDeck(deckIdForLog)) {
                deckName = "a custom deck";
            } else {
                deckName = PrebuiltDeck.findById(deckIdForLog).getName();
            }
            String playerName = gameData.playerIdToName.get(playerId);
            gameData.gameLog.add(playerName + " is playing with " + deckName + ".");
        }

        List<UUID> ids = new ArrayList<>(gameData.orderedPlayerIds);
        UUID startingPlayerId = ids.get(random.nextInt(ids.size()));
        String startingPlayerName = gameData.playerIdToName.get(startingPlayerId);
        gameData.startingPlayerId = startingPlayerId;

        gameData.gameLog.add(startingPlayerName + " wins the coin toss and goes first!");
        gameData.gameLog.add("Mulligan phase — decide to keep or mulligan.");

        log.info("Game {} - Mulligan phase begins. Starting player: {}", gameData.id, startingPlayerName);
    }

    private LobbyGame toLobbyGame(GameData data) {
        return new LobbyGame(
                data.id,
                data.gameName,
                data.createdByUsername,
                data.playerIds.size(),
                data.status
        );
    }
}

