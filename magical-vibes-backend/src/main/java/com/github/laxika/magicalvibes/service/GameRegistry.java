package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class GameRegistry {

    private final AtomicLong idCounter = new AtomicLong(1);
    private final Map<Long, GameData> games = new ConcurrentHashMap<>();

    public long nextId() {
        return idCounter.getAndIncrement();
    }

    public void register(GameData gameData) {
        games.put(gameData.id, gameData);
    }

    public GameData get(Long gameId) {
        return games.get(gameId);
    }

    public GameData getGameForPlayer(Long userId) {
        return games.values().stream()
                .filter(g -> g.playerIds.contains(userId))
                .findFirst()
                .orElse(null);
    }

    public Collection<GameData> getRunningGames() {
        return games.values().stream()
                .filter(g -> g.status != GameStatus.FINISHED)
                .toList();
    }
}
