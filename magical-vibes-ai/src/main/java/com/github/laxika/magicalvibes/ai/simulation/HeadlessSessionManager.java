package com.github.laxika.magicalvibes.ai.simulation;

import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.Connection;
import com.github.laxika.magicalvibes.networking.SessionManager;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Empty connection-state port for headless simulation.
 *
 * <p>Outbound methods fail immediately: simulation event batches are suppressed before subscriber
 * dispatch, so reaching either method is an architecture violation rather than harmless output.
 */
public final class HeadlessSessionManager implements SessionManager {

    @Override
    public void registerPlayer(Connection connection, UUID userId, String username) {
        throw new UnsupportedOperationException("Headless simulations have no player sessions");
    }

    @Override
    public void unregisterSession(String connectionId) {
        throw new UnsupportedOperationException("Headless simulations have no player sessions");
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
        throw new UnsupportedOperationException("Headless simulations have no player sessions");
    }

    @Override
    public boolean isInGame(String connectionId) {
        return false;
    }

    @Override
    public void clearInGame(String connectionId) {
        throw new UnsupportedOperationException("Headless simulations have no player sessions");
    }

    @Override
    public Collection<Player> getLobbyPlayers() {
        return List.of();
    }

    @Override
    public void sendToPlayer(UUID playerId, Object message) {
        throw new IllegalStateException("Headless simulation attempted transport output");
    }

    @Override
    public void sendToPlayers(Collection<UUID> playerIds, Object message) {
        throw new IllegalStateException("Headless simulation attempted transport output");
    }
}
