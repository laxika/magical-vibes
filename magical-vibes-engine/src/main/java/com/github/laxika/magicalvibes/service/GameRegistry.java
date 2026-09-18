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

    public GameData getActive(UUID gameId) {
        GameData game = get(gameId);
        if (game == null) return null;
        game.session.lock.lock();
        try { return game.session.active(); }
        finally { game.session.lock.unlock(); }
    }

    public GameData getGameForPlayer(UUID userId) {
        for (GameData g : games.values()) {
            if (g.session.isRoot(g) && g.playerIds.contains(userId) && g.status != GameStatus.FINISHED) {
                return getActive(g.id);
            }
        }
        return null;
    }

    public void remove(UUID gameId) {
        GameData game = games.get(gameId);
        if (game == null) return;
        game.session.lock.lock();
        try {
            if (game.session.isRoot(game)) {
                for (GameData frame : game.session.frames()) games.remove(frame.id);
            } else games.remove(gameId);
        } finally {
            game.session.lock.unlock();
        }
    }

    public Collection<GameData> getRunningGames() {
        return games.values().stream()
                .filter(g -> g.session.isRoot(g) && g.status != GameStatus.FINISHED)
                .toList();
    }
}

