package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.dto.GameResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
public class GameService {

    private final AtomicLong idCounter = new AtomicLong(1);
    private final Map<Long, GameData> games = new ConcurrentHashMap<>();

    public GameResponse createGame(String gameName, Long userId, String username) {
        long gameId = idCounter.getAndIncrement();

        GameData gameData = new GameData(gameId, gameName, userId, username);
        gameData.playerIds.add(userId);
        games.put(gameId, gameData);

        log.info("Game created: id={}, name='{}', creator={}", gameId, gameName, username);
        return toResponse(gameData);
    }

    public List<GameResponse> listRunningGames() {
        return games.values().stream()
                .filter(g -> "WAITING".equals(g.status))
                .map(this::toResponse)
                .toList();
    }

    public GameResponse joinGame(Long gameId, Long userId) {
        GameData gameData = games.get(gameId);
        if (gameData == null) {
            throw new IllegalArgumentException("Game not found");
        }

        if (!"WAITING".equals(gameData.status)) {
            throw new IllegalStateException("Game is not accepting players");
        }

        if (gameData.playerIds.contains(userId)) {
            throw new IllegalStateException("You are already in this game");
        }

        gameData.playerIds.add(userId);
        log.info("User {} joined game {}", userId, gameId);
        return toResponse(gameData);
    }

    private GameResponse toResponse(GameData data) {
        return new GameResponse(
                data.id,
                data.gameName,
                data.createdByUsername,
                data.status,
                data.createdAt,
                data.playerIds.size()
        );
    }

    private static class GameData {
        final long id;
        final String gameName;
        final long createdByUserId;
        final String createdByUsername;
        final LocalDateTime createdAt;
        String status;
        final Set<Long> playerIds = ConcurrentHashMap.newKeySet();

        GameData(long id, String gameName, long createdByUserId, String createdByUsername) {
            this.id = id;
            this.gameName = gameName;
            this.createdByUserId = createdByUserId;
            this.createdByUsername = createdByUsername;
            this.createdAt = LocalDateTime.now();
            this.status = "WAITING";
        }
    }
}
