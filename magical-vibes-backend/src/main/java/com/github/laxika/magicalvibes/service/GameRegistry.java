package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameRegistry {

    private final Map<UUID, GameData> games = new ConcurrentHashMap<>();

    public void register(GameData gameData) {
        games.put(gameData.id, gameData);
    }

    public GameData get(UUID gameId) {
        return games.get(gameId);
    }

    public GameData getGameForPlayer(UUID userId) {
        GameData fallback = null;
        for (GameData g : games.values()) {
            if (!g.playerIds.contains(userId)) continue;
            if (g.status == GameStatus.MULLIGAN || g.status == GameStatus.RUNNING) {
                return g;
            }
            if (fallback == null && g.status == GameStatus.WAITING) {
                fallback = g;
            }
        }
        return fallback;
    }

    public void remove(UUID gameId) {
        games.remove(gameId);
    }

    public Collection<GameData> getRunningGames() {
        return games.values().stream()
                .filter(g -> g.status != GameStatus.FINISHED)
                .toList();
    }
}
