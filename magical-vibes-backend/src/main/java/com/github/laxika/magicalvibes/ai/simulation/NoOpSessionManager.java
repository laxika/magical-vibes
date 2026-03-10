package com.github.laxika.magicalvibes.ai.simulation;

import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.Connection;
import com.github.laxika.magicalvibes.networking.SessionManager;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * No-op implementation of SessionManager that suppresses all network I/O during AI simulation.
 * Used by GameSimulator to run GameService logic without broadcasting to real players.
 */
public class NoOpSessionManager implements SessionManager {

    @Override
    public void registerPlayer(Connection connection, UUID userId, String username) {
        // no-op
    }

    @Override
    public void unregisterSession(String connectionId) {
        // no-op
    }

    @Override
    public Player getPlayer(String connectionId) {
        return null;
    }

    @Override
    public Connection getConnectionByUserId(UUID userId) {
        return null;
    }

    @Override
    public void setInGame(String connectionId) {
        // no-op
    }

    @Override
    public void clearInGame(String connectionId) {
        // no-op
    }

    @Override
    public Collection<Player> getLobbyPlayers() {
        return List.of();
    }

    @Override
    public void sendToPlayer(UUID playerId, Object message) {
        // no-op
    }

    @Override
    public void sendToPlayers(Collection<UUID> playerIds, Object message) {
        // no-op
    }
}
