package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.handler.GameMessageHandler;
import com.github.laxika.magicalvibes.model.*;
import com.github.laxika.magicalvibes.networking.Connection;
import com.github.laxika.magicalvibes.networking.MessageHandler;
import com.github.laxika.magicalvibes.networking.model.MessageType;
import com.github.laxika.magicalvibes.websocket.WebSocketSessionManager;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.mockito.Mockito.*;

class SubgameRequestContextTest {
    @Test
    void staleParentAndChildActionsAreRejectedAndCurrentEpochIsAccepted() throws Exception {
        Player player = new Player(UUID.randomUUID(), "Player");
        GameData root = new GameData(UUID.randomUUID(), "session", player.getId(), player.getUsername());
        root.status = GameStatus.RUNNING;
        root.playerIds.add(player.getId());
        GameRegistry registry = new GameRegistry();
        registry.register(root);
        WebSocketSessionManager sessions = mock(WebSocketSessionManager.class);
        Connection connection = mock(Connection.class);
        when(connection.getId()).thenReturn("connection");
        when(sessions.getPlayer("connection")).thenReturn(player);
        GameResyncProjectionService resync = mock(GameResyncProjectionService.class);
        ReconnectionService reconnect = mock(ReconnectionService.class);
        GameMessageHandler handler = new GameMessageHandler(null, null, resync, reconnect, null,
                registry, sessions, null, null, null, null, null, null, null, null, null);
        MessageHandler.GameRequest action = mock(MessageHandler.GameRequest.class);
        GameContext original = root.session.context();
        GameData child = new GameData(UUID.randomUUID(), "child", player.getId(), player.getUsername());
        root.session.push(child);
        GameContext childContext = root.session.context();

        handler.dispatchGameRequest(connection, original, action);
        handler.dispatchGameRequest(connection, null, action);
        verify(action, never()).run();
        verify(resync, times(2)).sendCurrentState(child, player.getId(), MessageType.GAME_JOINED);
        handler.dispatchGameRequest(connection, childContext, action);
        verify(action).run();

        root.session.pop();
        handler.dispatchGameRequest(connection, original, action);
        handler.dispatchGameRequest(connection, childContext, action);
        verify(action).run();
        handler.dispatchGameRequest(connection, root.session.context(), action);
        verify(action, times(2)).run();
    }
}
