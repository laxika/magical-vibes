package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.event.GameEventAudience;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.service.event.GameMutationCoordinator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReconnectionService {

    private final GameMutationCoordinator mutationCoordinator;

    public void resendAwaitingInput(GameData gameData, UUID playerId) {
        PendingInteraction active = gameData.interaction.activeInteraction();
        if (active != null) {
            UUID recipientId = decisionRecipient(gameData, active.decidingPlayerId());
            if (playerId.equals(recipientId)) {
                mutationCoordinator.emit(gameData,
                        new GameEventFact.DecisionRequested(
                                gameData.interaction.activeDecisionId(),
                                active.decidingPlayerId(),
                                decisionKind(active),
                                GameEventFact.DecisionDelivery.REPLAY_REQUESTED),
                        GameEventAudience.player(recipientId));
            }
            return;
        }

        UUID bottomDecisionId = gameData.playerBottomDecisionIds.get(playerId);
        if (bottomDecisionId != null && gameData.playerNeedsToBottom.containsKey(playerId)) {
            mutationCoordinator.emit(gameData,
                    new GameEventFact.DecisionRequested(
                            bottomDecisionId,
                            playerId,
                            GameEventFact.DecisionKind.CARDS_TO_BOTTOM,
                            GameEventFact.DecisionDelivery.REPLAY_REQUESTED),
                    GameEventAudience.player(playerId));
            return;
        }

        UUID mulliganDecisionId = gameData.playerMulliganDecisionIds.get(playerId);
        if (gameData.status == GameStatus.MULLIGAN
                && mulliganDecisionId != null
                && !gameData.playerKeptHand.contains(playerId)) {
            mutationCoordinator.emit(gameData,
                    new GameEventFact.DecisionRequested(
                            mulliganDecisionId,
                            playerId,
                            GameEventFact.DecisionKind.MULLIGAN,
                            GameEventFact.DecisionDelivery.REPLAY_REQUESTED),
                    GameEventAudience.player(playerId));
        }
    }

    private static UUID decisionRecipient(GameData gameData, UUID decidingPlayerId) {
        if (decidingPlayerId.equals(gameData.mindControlledPlayerId)
                && gameData.mindControllerPlayerId != null) {
            return gameData.mindControllerPlayerId;
        }
        return decidingPlayerId;
    }

    private static GameEventFact.DecisionKind decisionKind(PendingInteraction interaction) {
        if (interaction instanceof PendingInteraction.AttackerDeclaration) {
            return GameEventFact.DecisionKind.ATTACKER_DECLARATION;
        }
        if (interaction instanceof PendingInteraction.BlockerDeclaration) {
            return GameEventFact.DecisionKind.BLOCKER_DECLARATION;
        }
        if (interaction instanceof PendingInteraction.CombatDamageAssignment) {
            return GameEventFact.DecisionKind.COMBAT_DAMAGE_ASSIGNMENT;
        }
        return GameEventFact.DecisionKind.INTERACTION;
    }
}
