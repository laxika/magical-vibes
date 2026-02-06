package com.github.laxika.magicalvibes.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class WebSocketSessionManager {

    private final Map<String, Long> sessionToUserId = new ConcurrentHashMap<>();
    private final Map<String, String> sessionToUsername = new ConcurrentHashMap<>();
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public void registerSession(WebSocketSession session, Long userId, String username) {
        sessionToUserId.put(session.getId(), userId);
        sessionToUsername.put(session.getId(), username);
        sessions.put(session.getId(), session);
        log.info("Registered session {} for user {} ({})", session.getId(), userId, username);
    }

    public void unregisterSession(String sessionId) {
        Long userId = sessionToUserId.remove(sessionId);
        sessionToUsername.remove(sessionId);
        sessions.remove(sessionId);
        log.info("Unregistered session {} for user {}", sessionId, userId);
    }

    public Long getUserId(String sessionId) {
        return sessionToUserId.get(sessionId);
    }

    public String getUsername(String sessionId) {
        return sessionToUsername.get(sessionId);
    }

    public Map<String, WebSocketSession> getAllSessions() {
        return new ConcurrentHashMap<>(sessions);
    }
}
