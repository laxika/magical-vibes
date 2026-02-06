package com.github.laxika.magicalvibes.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class WebSocketSessionManager {

    // Maps session ID to user ID
    private final Map<String, Long> sessionToUser = new ConcurrentHashMap<>();

    // Maps session ID to WebSocketSession
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public void registerSession(WebSocketSession session, Long userId) {
        sessionToUser.put(session.getId(), userId);
        sessions.put(session.getId(), session);
        log.info("Registered session {} for user {}", session.getId(), userId);
    }

    public void unregisterSession(String sessionId) {
        Long userId = sessionToUser.remove(sessionId);
        sessions.remove(sessionId);
        log.info("Unregistered session {} for user {}", sessionId, userId);
    }

    public Long getUserId(String sessionId) {
        return sessionToUser.get(sessionId);
    }

    public Map<String, WebSocketSession> getAllSessions() {
        return new ConcurrentHashMap<>(sessions);
    }

    public int getActiveSessionCount() {
        return sessions.size();
    }
}
