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

        List<GameLogEntryView> newLogEntries = appendedLogEntries(gameData, batch);
        Set<UUID> logRecipients = new LinkedHashSet<>();

        for (GameEventEnvelope envelope : batch.events()) {
            Set<UUID> recipients = recipients(gameData, envelope.audience());
            if (recipients.isEmpty()) {
                continue;
            }

            if (envelope.fact() instanceof GameEventFact.StateInvalidated) {
                Set<UUID> recipientsWithLogs = new LinkedHashSet<>();
                Set<UUID> recipientsWithoutLogs = new LinkedHashSet<>();
                for (UUID recipient : recipients) {
                    if (newLogEntries.isEmpty() || !logRecipients.contains(recipient)) {
                        recipientsWithLogs.add(recipient);
                    } else {
                        recipientsWithoutLogs.add(recipient);
                    }
                }
                projectState(gameData, newLogEntries, recipientsWithLogs, logRecipients);
                projectState(gameData, List.of(), recipientsWithoutLogs, logRecipients);
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
    }

    private void projectState(
            GameData gameData,
            List<GameLogEntryView> logEntries,
            Set<UUID> recipients,
            Set<UUID> logRecipients) {
        if (recipients.isEmpty()) {
            return;
        }
        Map<UUID, GameStateMessage> messages =
                gameViewProjectionFactory.createGameStateMessages(
                        gameData, logEntries, recipients);
        for (UUID recipient : recipients) {
            GameStateMessage message = messages.get(recipient);
            if (message != null) {
                transport.sendToPlayer(recipient, message);
                logRecipients.add(recipient);
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
            transport.sendToPlayers(
                    authorizedRecipients,
                    interactionPromptProjectionRegistry.projectCardsToBottom(count));
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

    private List<GameLogEntryView> appendedLogEntries(
            GameData gameData, GameEventBatch batch) {
        List<Integer> indices = batch.events().stream()
                .map(GameEventEnvelope::fact)
                .filter(GameEventFact.GameLogAppended.class::isInstance)
                .map(GameEventFact.GameLogAppended.class::cast)
                .map(GameEventFact.GameLogAppended::logIndex)
                .toList();
        if (indices.isEmpty()) {
            return List.of();
        }
        return gameLogViewFactory.createAll(indices.stream()
                .map(index -> {
                    if (index >= gameData.gameLog.size()) {
                        throw new IllegalStateException(
                                "Game log event index " + index + " is outside authoritative state");
                    }
                    return gameData.gameLog.get(index);
                })
                .toList());
    }

    private Set<UUID> recipients(GameData gameData, GameEventAudience audience) {
        return switch (audience.visibility()) {
            case INTERNAL -> Set.of();
            case PUBLIC -> humanRecipients(gameData, gameData.orderedPlayerIds);
            case PRIVATE -> {
                yield humanRecipients(gameData, audience.playerIds());
            }
        };
    }

    private Set<UUID> humanRecipients(GameData gameData, Set<UUID> audiencePlayerIds) {
        LinkedHashSet<UUID> recipients = new LinkedHashSet<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (audiencePlayerIds.contains(playerId) && !gameData.aiPlayerIds.contains(playerId)) {
                recipients.add(playerId);
            }
        }
        return recipients;
    }

    private Set<UUID> humanRecipients(GameData gameData, List<UUID> audiencePlayerIds) {
        return humanRecipients(gameData, new LinkedHashSet<>(audiencePlayerIds));
    }
}
