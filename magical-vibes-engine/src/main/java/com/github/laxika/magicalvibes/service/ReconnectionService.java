package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.event.GameMutationCoordinator;
import com.github.laxika.magicalvibes.service.event.InteractionPromptProjectionRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Replays only the currently authoritative decision to an authorized reconnecting player.
 *
 * <p>Reconnect is an observation, not a game mutation: it reuses the canonical interaction
 * projection registry directly and never allocates an event, state version, sequence, or new
 * decision identity.
 */
@Service
@RequiredArgsConstructor
public class ReconnectionService {

    private final GameMutationCoordinator mutationCoordinator;
    private final InteractionPromptProjectionRegistry interactionPromptProjectionRegistry;
    private final GameMessageTransport transport;

    public void resendAwaitingInput(GameData gameData, UUID playerId) {
        mutationCoordinator.observe(
                gameData,
                () -> currentReplay(gameData, playerId),
                replay -> {
                    if (replay != null) {
                        transport.sendToPlayer(replay.recipientId(), replay.message());
                    }
                });
    }

    private Replay currentReplay(GameData gameData, UUID reconnectingPlayerId) {
        if (gameData.simulation) {
            return null;
        }

        PendingInteraction active = gameData.interaction.activeInteraction();
        if (active != null) {
            UUID recipientId = decisionRecipient(gameData, active.decidingPlayerId());
            if (!reconnectingPlayerId.equals(recipientId)
                    || gameData.interaction.activeDecisionId() == null) {
                return null;
            }
            return interactionPromptProjectionRegistry.project(gameData, active)
                    .map(message -> new Replay(recipientId, message))
                    .orElse(null);
        }

        UUID bottomDecisionId = gameData.playerBottomDecisionIds.get(reconnectingPlayerId);
        Integer bottomCount = gameData.playerNeedsToBottom.get(reconnectingPlayerId);
        if (bottomDecisionId != null && bottomCount != null) {
            return new Replay(
                    reconnectingPlayerId,
                    interactionPromptProjectionRegistry.projectCardsToBottom(bottomCount));
        }

        return null;
    }

    private static UUID decisionRecipient(GameData gameData, UUID decidingPlayerId) {
        if (decidingPlayerId.equals(gameData.mindControlledPlayerId)
                && gameData.mindControllerPlayerId != null) {
            return gameData.mindControllerPlayerId;
        }
        return decidingPlayerId;
    }

    private record Replay(UUID recipientId, Object message) {
    }
}
