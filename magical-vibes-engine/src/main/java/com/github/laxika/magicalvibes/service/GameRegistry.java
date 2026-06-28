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
        for (GameData g : games.values()) {
            if (g.playerIds.contains(userId) && g.status != GameStatus.FINISHED) {
                return g;
            }
        }
        return null;
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

