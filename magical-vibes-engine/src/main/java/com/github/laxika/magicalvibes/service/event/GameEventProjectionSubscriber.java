package com.github.laxika.magicalvibes.service.event;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.event.GameEventAudience;
import com.github.laxika.magicalvibes.model.event.GameEventBatch;
import com.github.laxika.magicalvibes.model.event.GameEventEnvelope;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.networking.message.GameOverMessage;
import com.github.laxika.magicalvibes.networking.message.GameStateMessage;
import com.github.laxika.magicalvibes.networking.message.MulliganResolvedMessage;
import com.github.laxika.magicalvibes.networking.message.SelectCardsToBottomMessage;
import com.github.laxika.magicalvibes.networking.model.GameLogEntryView;
import com.github.laxika.magicalvibes.networking.service.GameLogViewFactory;
import com.github.laxika.magicalvibes.service.GameMessageTransport;
import com.github.laxika.magicalvibes.service.GameRegistry;
import com.github.laxika.magicalvibes.service.GameViewProjectionFactory;
import com.github.laxika.magicalvibes.service.PrivateInformationProjectionFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Canonical adapter from completed internal game events to the existing typed wire protocol.
 *
 * <p>It resolves authoritative state by game ID only after mutation dispatch, enforces every
 * envelope audience, builds recipient-specific views, and delegates delivery. It never serializes.
 */
@Slf4j
@Component
@Order(0)
@RequiredArgsConstructor
public class GameEventProjectionSubscriber implements GameEventSubscriber {

    private final GameRegistry gameRegistry;
    private final GameViewProjectionFactory gameViewProjectionFactory;
    private final PrivateInformationProjectionFactory privateInformationProjectionFactory;
    private final InteractionPromptProjectionRegistry interactionPromptProjectionRegistry;
    private final GameLogViewFactory gameLogViewFactory;
    private final GameMessageTransport transport;

    @Override
    public void onGameEvents(GameEventBatch batch) {
        GameData gameData = gameRegistry.get(batch.gameId());
        if (gameData == null) {
            log.warn("Cannot project game event batch {}: game {} is not registered",
                    batch.causalActionId(), batch.gameId());
            return;
        }
        if (Thread.holdsLock(gameData)) {
            throw new IllegalStateException("Game event projection must run outside the game monitor");
        }

        List<GameLogEntryView> newLogEntries = pendingLogEntries(gameData);
        Map<UUID, GameStateMessage> stateMessagesWithLogs = null;
        Map<UUID, GameStateMessage> stateMessagesWithoutLogs = null;
        Set<UUID> logRecipients = new LinkedHashSet<>();
        boolean stateProjected = false;

        for (GameEventEnvelope envelope : batch.events()) {
            Set<UUID> recipients = recipients(gameData, envelope.audience());
            if (recipients.isEmpty()) {
                continue;
            }

            if (envelope.fact() instanceof GameEventFact.StateInvalidated) {
                if (stateMessagesWithLogs == null) {
                    stateMessagesWithLogs = gameViewProjectionFactory.createGameStateMessages(
                            gameData, newLogEntries);
                }
                for (UUID recipient : recipients) {
                    boolean includeLogs = newLogEntries.isEmpty() || !logRecipients.contains(recipient);
                    Map<UUID, GameStateMessage> messages = stateMessagesWithLogs;
                    if (!includeLogs) {
                        if (stateMessagesWithoutLogs == null) {
                            stateMessagesWithoutLogs =
                                    gameViewProjectionFactory.createGameStateMessages(
                                            gameData, List.of());
                        }
                        messages = stateMessagesWithoutLogs;
                    }
                    GameStateMessage message = messages.get(recipient);
                    if (message != null) {
                        transport.sendToPlayer(recipient, message);
                        logRecipients.add(recipient);
                    }
                }
                stateProjected = true;
            } else if (envelope.fact() instanceof GameEventFact.DecisionRequested decision) {
                projectDecision(gameData, decision, recipients);
            } else if (envelope.fact() instanceof GameEventFact.PrivateReveal reveal) {
                Object message = privateInformationProjectionFactory.createReveal(gameData, reveal);
                transport.sendToPlayers(recipients, message);
            } else if (envelope.fact() instanceof GameEventFact.MulliganResolved mulligan) {
                String playerName = gameData.playerIdToName.get(mulligan.playerId());
                transport.sendToPlayers(recipients, new MulliganResolvedMessage(
                        playerName, mulligan.kept(), mulligan.mulliganCount()));
            } else if (envelope.fact() instanceof GameEventFact.GameEnded ended) {
                UUID winnerId = ended.winnerPlayerId();
                String winnerName = winnerId == null ? null : gameData.playerIdToName.get(winnerId);
                transport.sendToPlayers(recipients, new GameOverMessage(winnerId, winnerName));
            }
        }

        if (stateProjected) {
            // This delivery cursor remains on GameData only for legacy parity. Projection itself
            // is complete before this brief synchronized bookkeeping update.
            synchronized (gameData) {
                gameData.lastBroadcastedLogSize = gameData.gameLog.size();
            }
        }
    }

    private void projectDecision(
            GameData gameData, GameEventFact.DecisionRequested decision, Set<UUID> recipients) {
        Set<UUID> authorizedRecipients = decisionRecipients(gameData, decision, recipients);
        if (authorizedRecipients.isEmpty()) {
            return;
        }
        if (decision.decisionKind() == GameEventFact.DecisionKind.CARDS_TO_BOTTOM) {
            Integer count = gameData.playerNeedsToBottom.get(decision.decidingPlayerId());
            if (count == null) {
                log.warn("Cannot project cards-to-bottom decision {}: no pending count for player {}",
                        decision.decisionId(), decision.decidingPlayerId());
                return;
            }
            transport.sendToPlayers(authorizedRecipients, new SelectCardsToBottomMessage(count));
            return;
        }

        if (decision.decisionKind() == GameEventFact.DecisionKind.MULLIGAN) {
            return;
        }

        if (!activeInteractionMatches(gameData, decision)) {
            log.warn("Cannot project interaction decision {}: active interaction does not match {}",
                    decision.decisionId(), decision.decisionKind());
            return;
        }
        PendingInteraction active = gameData.interaction.activeInteraction();
        var prompt = interactionPromptProjectionRegistry.project(gameData, active);
        if (prompt.isPresent()) {
            transport.sendToPlayers(authorizedRecipients, prompt.get());
        }
    }

    private Set<UUID> decisionRecipients(
            GameData gameData,
            GameEventFact.DecisionRequested decision,
            Set<UUID> audienceRecipients) {
        UUID decisionOwner = decision.decidingPlayerId();
        if (decisionOwner.equals(gameData.mindControlledPlayerId)
                && gameData.mindControllerPlayerId != null) {
            decisionOwner = gameData.mindControllerPlayerId;
        }
        return audienceRecipients.contains(decisionOwner) ? Set.of(decisionOwner) : Set.of();
    }

    private boolean activeInteractionMatches(
            GameData gameData, GameEventFact.DecisionRequested decision) {
        var active = gameData.interaction.activeInteraction();
        if (active == null
                || !decision.decisionId().equals(gameData.interaction.activeDecisionId())
                || !active.decidingPlayerId().equals(decision.decidingPlayerId())) {
            return false;
        }
        return switch (decision.decisionKind()) {
            case INTERACTION -> !(active instanceof PendingInteraction.AttackerDeclaration)
                    && !(active instanceof PendingInteraction.BlockerDeclaration)
                    && !(active instanceof PendingInteraction.CombatDamageAssignment);
            case ATTACKER_DECLARATION ->
                    active instanceof PendingInteraction.AttackerDeclaration;
            case BLOCKER_DECLARATION ->
                    active instanceof PendingInteraction.BlockerDeclaration;
            case COMBAT_DAMAGE_ASSIGNMENT ->
                    active instanceof PendingInteraction.CombatDamageAssignment;
            case MULLIGAN, CARDS_TO_BOTTOM -> false;
        };
    }

    private List<GameLogEntryView> pendingLogEntries(GameData gameData) {
        int logSize = gameData.gameLog.size();
        if (logSize <= gameData.lastBroadcastedLogSize) {
            return List.of();
        }
        return gameLogViewFactory.createAll(
                gameData.gameLog.subList(gameData.lastBroadcastedLogSize, logSize));
    }

    private Set<UUID> recipients(GameData gameData, GameEventAudience audience) {
        return switch (audience.visibility()) {
            case INTERNAL -> Set.of();
            case PUBLIC -> new LinkedHashSet<>(gameData.orderedPlayerIds);
            case PRIVATE -> {
                LinkedHashSet<UUID> recipients = new LinkedHashSet<>();
                for (UUID playerId : gameData.orderedPlayerIds) {
                    if (audience.playerIds().contains(playerId)) {
                        recipients.add(playerId);
                    }
                }
                yield recipients;
            }
        };
    }
}
