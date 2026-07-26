package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.networking.message.JoinGame;
import com.github.laxika.magicalvibes.networking.message.JoinGameMessage;
import com.github.laxika.magicalvibes.networking.model.MessageType;
import com.github.laxika.magicalvibes.service.event.GameMutationCoordinator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Builds a complete current-state projection for join and reconnect workflows.
 *
 * <p>This is deliberately a read-side command, not a fake mutation event and not historical
 * replay. The returned view is derived from the authoritative state while holding its monitor.
 */
@Service
@RequiredArgsConstructor
public class GameResyncProjectionService {

    private final GameViewProjectionFactory projectionFactory;
    private final GameMessageTransport transport;
    private final GameMutationCoordinator mutationCoordinator;

    public JoinGame currentState(GameData gameData, UUID playerId) {
        return mutationCoordinator.observe(
                gameData, () -> projectionFactory.getJoinGame(gameData, playerId));
    }

    public void sendCurrentState(GameData gameData, UUID playerId, MessageType messageType) {
        mutationCoordinator.observe(
                gameData,
                () -> projectionFactory.getJoinGame(gameData, playerId),
                current -> transport.sendToPlayer(
                        playerId, new JoinGameMessage(messageType, current)));
    }
}
