package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.cards.CardSet;
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
    private final GameService gameService;

    public GameResult createGame(String gameName, Player player) {
        UUID gameId = UUID.randomUUID();

        GameData gameData = new GameData(gameId, gameName, player.getId(), player.getUsername());
        gameData.playerIds.add(player.getId());
        gameData.orderedPlayerIds.add(player.getId());
        gameData.playerNames.add(player.getUsername());
        gameData.playerIdToName.put(player.getId(), player.getUsername());
        gameRegistry.register(gameData);

        log.info("Game created: id={}, name='{}', creator={}", gameId, gameName, player.getUsername());
        return new GameResult(gameService.getJoinGame(gameData, null), toLobbyGame(gameData));
    }

    public List<LobbyGame> listRunningGames() {
        return gameRegistry.getRunningGames().stream()
                .map(this::toLobbyGame)
                .toList();
    }

    public LobbyGame joinGame(GameData gameData, Player player) {
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

        if (gameData.playerIds.size() >= 2) {
            initializeGame(gameData);
        }

        log.info("User {} joined game {}, status={}", player.getUsername(), gameData.id, gameData.status);
        return toLobbyGame(gameData);
    }

    private void initializeGame(GameData gameData) {
        Card forest = CardSet.TENTH_EDITION.findByCollectorNumber("380").createCard();
        Card llanowarElves = CardSet.TENTH_EDITION.findByCollectorNumber("268").createCard();
        Card grizzlyBears = CardSet.TENTH_EDITION.findByCollectorNumber("246").createCard();
        Card giantSpider = CardSet.TENTH_EDITION.findByCollectorNumber("187").createCard();
        Card huntedWumpus = CardSet.TENTH_EDITION.findByCollectorNumber("178").createCard();
        Card mightOfOaks = CardSet.TENTH_EDITION.findByCollectorNumber("270").createCard();

        for (UUID playerId : gameData.playerIds) {
            List<Card> deck = new ArrayList<>();
            for (int i = 0; i < 24; i++) {
                deck.add(forest);
            }
            for (int i = 0; i < 4; i++) {
                deck.add(llanowarElves);
            }
            for (int i = 0; i < 4; i++) {
                deck.add(giantSpider);
            }
            for (int i = 0; i < 4; i++) {
                deck.add(huntedWumpus);
            }
            for (int i = 0; i < 20; i++) {
                deck.add(grizzlyBears);
            }
            for (int i = 0; i < 4; i++) {
                deck.add(mightOfOaks);
            }
            Collections.shuffle(deck, random);
            gameData.playerDecks.put(playerId, deck);
            gameData.mulliganCounts.put(playerId, 0);
            gameData.playerBattlefields.put(playerId, new ArrayList<>());
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
        gameData.gameLog.add("Each player receives a 10th Edition deck: 24 Forests, 4 Llanowar Elves, 4 Giant Spiders, 4 Hunted Wumpuses, 20 Grizzly Bears, and 4 Might of Oaks.");

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
