package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.Player;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class WebSocketSessionManager {

    private final Map<String, Player> players = new ConcurrentHashMap<>();
    private final Map<Long, String> userIdToSessionId = new ConcurrentHashMap<>();
    private final Set<String> inGameSessionIds = ConcurrentHashMap.newKeySet();

    public void registerPlayer(WebSocketSession session, Long userId, String username) {
        Player player = new Player(userId, username, session);
        players.put(session.getId(), player);
        userIdToSessionId.put(userId, session.getId());
        log.info("Registered session {} for user {} ({})", session.getId(), userId, username);
    }

    public void unregisterSession(String sessionId) {
        Player player = players.remove(sessionId);
        inGameSessionIds.remove(sessionId);
        if (player != null) {
            userIdToSessionId.remove(player.getId());
            log.info("Unregistered session {} for user {} ({})", sessionId, player.getId(), player.getUsername());
        }
    }

    public Player getPlayer(String sessionId) {
        return players.get(sessionId);
    }

    public Player getPlayerByUserId(Long userId) {
        String sessionId = userIdToSessionId.get(userId);
        return sessionId != null ? players.get(sessionId) : null;
    }

    public void setInGame(String sessionId) {
        inGameSessionIds.add(sessionId);
    }

    public Collection<Player> getLobbyPlayers() {
        return players.entrySet().stream()
                .filter(e -> !inGameSessionIds.contains(e.getKey()))
                .map(Map.Entry::getValue)
                .toList();
    }
}
