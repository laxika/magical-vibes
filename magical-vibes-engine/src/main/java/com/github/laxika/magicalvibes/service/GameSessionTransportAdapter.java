package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.networking.Connection;
import com.github.laxika.magicalvibes.networking.SessionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Explicit adapter for engine reads of transport connection state.
 *
 * <p>Mutation services depend on these semantic connection queries rather than reaching into
 * {@link SessionManager}. Typed outbound message delivery remains in {@link GameMessageTransport}.
 */
@Component
@RequiredArgsConstructor
public class GameSessionTransportAdapter implements PlayerConnectionState {

    private final SessionManager sessionManager;

    @Override
    public boolean isPlayerConnected(UUID playerId) {
        Connection connection = sessionManager.getConnectionByUserId(playerId);
        return connection != null && sessionManager.isInGame(connection.getId());
    }
}
