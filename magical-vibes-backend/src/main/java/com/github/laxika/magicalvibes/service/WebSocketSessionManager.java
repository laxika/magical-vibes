package com.github.laxika.magicalvibes.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WebSocketSessionManager {

    private final Map<String, Long> sessionToUserId = new ConcurrentHashMap<>();
    private final Map<String, String> sessionToUsername = new ConcurrentHashMap<>();
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, Long> sessionToGameId = new ConcurrentHashMap<>();
    private final Map<Long, String> userIdToSessionId = new ConcurrentHashMap<>();

    public void registerSession(WebSocketSession session, Long userId, String username) {
        sessionToUserId.put(session.getId(), userId);
        sessionToUsername.put(session.getId(), username);
        sessions.put(session.getId(), session);
        userIdToSessionId.put(userId, session.getId());
        log.info("Registered session {} for user {} ({})", session.getId(), userId, username);
    }

    public void unregisterSession(String sessionId) {
        Long userId = sessionToUserId.remove(sessionId);
        sessionToUsername.remove(sessionId);
        sessions.remove(sessionId);
        sessionToGameId.remove(sessionId);
        if (userId != null) {
            userIdToSessionId.remove(userId);
        }
        log.info("Unregistered session {} for user {}", sessionId, userId);
    }

    public Long getUserId(String sessionId) {
        return sessionToUserId.get(sessionId);
    }

    public String getUsername(String sessionId) {
        return sessionToUsername.get(sessionId);
    }

    public void setInGame(String sessionId, Long gameId) {
        sessionToGameId.put(sessionId, gameId);
    }

    public WebSocketSession getSessionByUserId(Long userId) {
        String sessionId = userIdToSessionId.get(userId);
        return sessionId != null ? sessions.get(sessionId) : null;
    }

    public Map<String, WebSocketSession> getLobbySessions() {
        return sessions.entrySet().stream()
                .filter(e -> !sessionToGameId.containsKey(e.getKey()))
                .collect(Collectors.toConcurrentMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<String, WebSocketSession> getAllSessions() {
        return new ConcurrentHashMap<>(sessions);
    }
}
