package com.github.laxika.magicalvibes.ai.simulation;

import com.github.laxika.magicalvibes.websocket.WebSocketSessionManager;
import tools.jackson.databind.ObjectMapper;

import java.util.Collection;
import java.util.UUID;

/**
 * WebSocket session manager for headless AI simulation: extends the real manager so
 * {@link com.github.laxika.magicalvibes.service.GameTimeoutService} can be wired, but
 * suppresses all outbound messages.
 */
public class HeadlessWebSocketSessionManager extends WebSocketSessionManager {

    public HeadlessWebSocketSessionManager(ObjectMapper objectMapper) {
        super(objectMapper);
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
