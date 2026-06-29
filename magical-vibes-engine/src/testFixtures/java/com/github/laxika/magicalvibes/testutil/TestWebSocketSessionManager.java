package com.github.laxika.magicalvibes.testutil;

import com.github.laxika.magicalvibes.networking.Connection;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.websocket.WebSocketSessionManager;
import tools.jackson.databind.ObjectMapper;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class TestWebSocketSessionManager extends WebSocketSessionManager {

    private static final VarHandle PLAYERS_HANDLE;
    private static final VarHandle CONNECTIONS_HANDLE;
    private static final VarHandle USER_ID_TO_CONN_HANDLE;
    private static final VarHandle IN_GAME_HANDLE;

    static {
        try {
            var lookup = MethodHandles.privateLookupIn(WebSocketSessionManager.class, MethodHandles.lookup());
            PLAYERS_HANDLE = lookup.findVarHandle(WebSocketSessionManager.class, "players", Map.class);
            CONNECTIONS_HANDLE = lookup.findVarHandle(WebSocketSessionManager.class, "connections", Map.class);
            USER_ID_TO_CONN_HANDLE = lookup.findVarHandle(WebSocketSessionManager.class, "userIdToConnectionId", Map.class);
            IN_GAME_HANDLE = lookup.findVarHandle(WebSocketSessionManager.class, "inGameConnectionIds", Set.class);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public TestWebSocketSessionManager(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @SuppressWarnings("unchecked")
    public void reset() {
        ((Map<String, Player>) PLAYERS_HANDLE.get(this)).clear();
        ((Map<String, Connection>) CONNECTIONS_HANDLE.get(this)).clear();
        ((Map<UUID, String>) USER_ID_TO_CONN_HANDLE.get(this)).clear();
        ((Set<String>) IN_GAME_HANDLE.get(this)).clear();
    }
}
