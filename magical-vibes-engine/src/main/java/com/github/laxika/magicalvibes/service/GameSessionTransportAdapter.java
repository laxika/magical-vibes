package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.networking.Connection;
import com.github.laxika.magicalvibes.networking.SessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Explicit adapter for engine reads and lifecycle operations on transport sessions.
 *
 * <p>Mutation services depend on these semantic connection queries rather than reaching into
 * {@link SessionManager}. Typed outbound message delivery remains in {@link GameMessageTransport}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GameSessionTransportAdapter {

    private static final String AI_CONNECTION_PREFIX = "ai-";

    private final SessionManager sessionManager;

    public boolean isAiPlayer(UUID playerId) {
        Connection connection = sessionManager.getConnectionByUserId(playerId);
        return connection != null && connection.getId().startsWith(AI_CONNECTION_PREFIX);
    }

    public boolean isPlayerConnected(UUID playerId) {
        Connection connection = sessionManager.getConnectionByUserId(playerId);
        return connection != null && sessionManager.isInGame(connection.getId());
    }

    public void closeAiConnections(Collection<UUID> playerIds, UUID gameId) {
        Set<String> closedConnectionIds = new HashSet<>();
        for (UUID playerId : playerIds) {
            Connection connection = sessionManager.getConnectionByUserId(playerId);
            if (connection == null
                    || !connection.getId().startsWith(AI_CONNECTION_PREFIX)
                    || !closedConnectionIds.add(connection.getId())) {
                continue;
            }
            try {
                connection.close();
            } catch (Exception failure) {
                log.warn("Failed to close AI connection {} for game {}",
                        connection.getId(), gameId, failure);
            } finally {
                sessionManager.unregisterSession(connection.getId());
            }
        }
    }
}
