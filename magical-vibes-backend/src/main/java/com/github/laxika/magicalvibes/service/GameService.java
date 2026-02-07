package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.dto.GameResponse;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Player;
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
                data.startingPlayerName
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
