package com.github.laxika.magicalvibes.websocket;

import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.Connection;
import com.github.laxika.magicalvibes.networking.SessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class WebSocketSessionManager implements SessionManager {

    private final ObjectMapper objectMapper;

    private final Map<String, Player> players = new ConcurrentHashMap<>();
    private final Map<String, Connection> connections = new ConcurrentHashMap<>();
    private final Map<Long, String> userIdToConnectionId = new ConcurrentHashMap<>();
    private final Set<String> inGameConnectionIds = ConcurrentHashMap.newKeySet();

    public void registerPlayer(Connection connection, Long userId, String username) {
        Player player = new Player(userId, username);
        players.put(connection.getId(), player);
        connections.put(connection.getId(), connection);
        userIdToConnectionId.put(userId, connection.getId());
        log.info("Registered connection {} for user {} ({})", connection.getId(), userId, username);
    }

    public void unregisterSession(String connectionId) {
        Player player = players.remove(connectionId);
        connections.remove(connectionId);
        inGameConnectionIds.remove(connectionId);
        if (player != null) {
            userIdToConnectionId.remove(player.getId());
            log.info("Unregistered connection {} for user {} ({})", connectionId, player.getId(), player.getUsername());
        }
    }

    public Player getPlayer(String connectionId) {
        return players.get(connectionId);
    }

    public Connection getConnectionByUserId(Long userId) {
        String connectionId = userIdToConnectionId.get(userId);
        return connectionId != null ? connections.get(connectionId) : null;
    }

    public void setInGame(String connectionId) {
        inGameConnectionIds.add(connectionId);
    }

    public Collection<Player> getLobbyPlayers() {
        return players.entrySet().stream()
                .filter(e -> !inGameConnectionIds.contains(e.getKey()))
                .map(Map.Entry::getValue)
                .toList();
    }

    @Override
    public void sendToPlayer(Long playerId, Object message) {
        String json;
        try {
            json = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            log.error("Error serializing message", e);
            return;
        }
        Connection connection = getConnectionByUserId(playerId);
        if (connection != null && connection.isOpen()) {
            try {
                connection.sendMessage(json);
            } catch (Exception e) {
                log.error("Error sending message to player {}", playerId, e);
            }
        }
    }

    @Override
    public void sendToPlayers(Collection<Long> playerIds, Object message) {
        String json;
        try {
            json = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            log.error("Error serializing message", e);
            return;
        }
        for (Long playerId : playerIds) {
            Connection connection = getConnectionByUserId(playerId);
            if (connection != null && connection.isOpen()) {
                try {
                    connection.sendMessage(json);
                } catch (Exception e) {
                    log.error("Error sending message to player {}", playerId, e);
                }
            }
        }
    }
}
