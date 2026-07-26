package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.networking.SessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.UUID;

/**
 * Typed-message delivery boundary for engine output.
 *
 * <p>Projection code never talks to connections directly. Failures are isolated per recipient so
 * one broken connection cannot prevent delivery to another player.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GameMessageTransport {

    private final SessionManager sessionManager;

    public void sendToPlayer(UUID playerId, Object message) {
        try {
            sessionManager.sendToPlayer(playerId, message);
        } catch (VirtualMachineError fatal) {
            throw fatal;
        } catch (Throwable failure) {
            log.error("Failed to deliver {} to player {}",
                    message.getClass().getSimpleName(), playerId, failure);
        }
    }

    public void sendToPlayers(Collection<UUID> playerIds, Object message) {
        for (UUID playerId : playerIds) {
            sendToPlayer(playerId, message);
        }
    }
}
