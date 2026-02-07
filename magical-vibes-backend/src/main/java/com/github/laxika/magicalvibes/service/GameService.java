package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.dto.GameResponse;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Slf4j
public class GameService {

    private final AtomicLong idCounter = new AtomicLong(1);
    private final Map<Long, GameData> games = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public GameResponse createGame(String gameName, Player player) {
        long gameId = idCounter.getAndIncrement();

        GameData gameData = new GameData(gameId, gameName, player.getId(), player.getUsername());
        gameData.playerIds.add(player.getId());
        gameData.playerNames.add(player.getUsername());
        games.put(gameId, gameData);

        log.info("Game created: id={}, name='{}', creator={}", gameId, gameName, player.getUsername());
        return toResponse(gameData);
    }

    public List<GameResponse> listRunningGames() {
        return games.values().stream()
                .filter(g -> g.status != GameStatus.FINISHED)
                .map(this::toResponse)
                .toList();
    }

    public GameResponse joinGame(Long gameId, Player player) {
        GameData gameData = games.get(gameId);
        if (gameData == null) {
            throw new IllegalArgumentException("Game not found");
        }

        if (gameData.status != GameStatus.WAITING) {
            throw new IllegalStateException("Game is not accepting players");
        }

        if (gameData.playerIds.contains(player.getId())) {
            throw new IllegalStateException("You are already in this game");
        }

        gameData.playerIds.add(player.getId());
        gameData.playerNames.add(player.getUsername());

        if (gameData.playerIds.size() >= 2) {
            gameData.status = GameStatus.RUNNING;
            initializeGame(gameData);
        }

        log.info("User {} joined game {}, status={}", player.getUsername(), gameId, gameData.status);
        return toResponse(gameData);
    }

    public Long getCreatorUserId(Long gameId) {
        GameData gameData = games.get(gameId);
        return gameData != null ? gameData.createdByUserId : null;
    }

    private void initializeGame(GameData gameData) {
        Card forest = new Card("Forest", "Basic Land", "Forest", "G");

        for (Long playerId : gameData.playerIds) {
            List<Card> deck = IntStream.range(0, 60)
                    .mapToObj(i -> forest)
                    .collect(Collectors.toList());
            gameData.playerDecks.put(playerId, deck);
        }

        gameData.gameLog.add("Game started!");
        gameData.gameLog.add("Each player receives a deck of 60 Forests.");

        List<String> names = new ArrayList<>(gameData.playerNames);
        String startingPlayer = names.get(random.nextInt(names.size()));
        gameData.startingPlayerName = startingPlayer;

        gameData.gameLog.add(startingPlayer + " wins the coin toss and goes first!");

        gameData.activePlayerName = startingPlayer;
        gameData.turnNumber = 1;
        gameData.currentStep = TurnStep.first();

        log.info("Game {} - Turn 1 begins. Active player: {}, Step: {}", gameData.id, startingPlayer, gameData.currentStep);
    }

    public GameResponse passPriority(Long gameId, Player player) {
        GameData gameData = games.get(gameId);
        if (gameData == null) {
            throw new IllegalArgumentException("Game not found");
        }
        if (gameData.status != GameStatus.RUNNING) {
            throw new IllegalStateException("Game is not running");
        }

        synchronized (gameData) {
            gameData.priorityPassedBy.add(player.getUsername());
            log.info("Game {} - {} passed priority on step {} (passed: {}/2)",
                    gameId, player.getUsername(), gameData.currentStep, gameData.priorityPassedBy.size());

            if (gameData.priorityPassedBy.size() >= 2) {
                advanceStep(gameData);
            }
        }

        return toResponse(gameData);
    }

    private void advanceStep(GameData gameData) {
        gameData.priorityPassedBy.clear();
        TurnStep next = gameData.currentStep.next();

        if (next != null) {
            gameData.currentStep = next;
            gameData.gameLog.add("Step: " + next.getDisplayName());
            log.info("Game {} - Step advanced to {}", gameData.id, next);
        } else {
            advanceTurn(gameData);
        }
    }

    private void advanceTurn(GameData gameData) {
        List<String> names = new ArrayList<>(gameData.playerNames);
        String currentActive = gameData.activePlayerName;
        String nextActive = names.get(0).equals(currentActive) ? names.get(1) : names.get(0);

        gameData.activePlayerName = nextActive;
        gameData.turnNumber++;
        gameData.currentStep = TurnStep.first();
        gameData.priorityPassedBy.clear();

        gameData.gameLog.add("Turn " + gameData.turnNumber + " begins. " + nextActive + "'s turn.");
        log.info("Game {} - Turn {} begins. Active player: {}", gameData.id, gameData.turnNumber, nextActive);
    }

    public Long getGameIdForPlayer(Long userId) {
        return games.entrySet().stream()
                .filter(e -> e.getValue().playerIds.contains(userId))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    public Set<Long> getPlayerIds(Long gameId) {
        GameData gameData = games.get(gameId);
        return gameData != null ? new HashSet<>(gameData.playerIds) : Set.of();
    }

    private GameResponse toResponse(GameData data) {
        return new GameResponse(
                data.id,
                data.gameName,
                data.createdByUsername,
                data.status,
                data.createdAt,
                data.playerIds.size(),
                new ArrayList<>(data.playerNames),
                new ArrayList<>(data.gameLog),
                data.startingPlayerName,
                data.currentStep,
                data.activePlayerName,
                data.turnNumber
        );
    }

    private static class GameData {
        final long id;
        final String gameName;
        final long createdByUserId;
        final String createdByUsername;
        final LocalDateTime createdAt;
        GameStatus status;
        final Set<Long> playerIds = ConcurrentHashMap.newKeySet();
        final List<String> playerNames = Collections.synchronizedList(new ArrayList<>());
        final Map<Long, List<Card>> playerDecks = new ConcurrentHashMap<>();
        final List<String> gameLog = Collections.synchronizedList(new ArrayList<>());
        String startingPlayerName;
        TurnStep currentStep;
        String activePlayerName;
        int turnNumber;
        final Set<String> priorityPassedBy = ConcurrentHashMap.newKeySet();

        GameData(long id, String gameName, long createdByUserId, String createdByUsername) {
            this.id = id;
            this.gameName = gameName;
            this.createdByUserId = createdByUserId;
            this.createdByUsername = createdByUsername;
            this.createdAt = LocalDateTime.now();
            this.status = GameStatus.WAITING;
        }
    }
}
