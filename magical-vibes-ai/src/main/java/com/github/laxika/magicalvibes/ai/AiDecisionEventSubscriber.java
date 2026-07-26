package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.event.GameEventAudience;
import com.github.laxika.magicalvibes.model.event.GameEventBatch;
import com.github.laxika.magicalvibes.model.event.GameEventEnvelope;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.service.event.GameEventSubscriber;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Wakes live AI players directly from completed, transport-independent game facts.
 *
 * <p>The subscriber only schedules decision kinds. Delayed execution reads authoritative live
 * game state through the registered decision engine. Simulation batches never reach subscribers.
 */
@Component
@Order(-100)
public class AiDecisionEventSubscriber implements GameEventSubscriber {

    private final Map<UUID, Map<UUID, AiDecisionScheduler>> schedulersByGame =
            new ConcurrentHashMap<>();

    public void register(UUID gameId, UUID playerId, AiDecisionScheduler scheduler) {
        schedulersByGame
                .computeIfAbsent(gameId, ignored -> new ConcurrentHashMap<>())
                .put(playerId, scheduler);
    }

    public void unregister(UUID gameId, UUID playerId) {
        schedulersByGame.computeIfPresent(gameId, (ignored, schedulers) -> {
            schedulers.remove(playerId);
            return schedulers.isEmpty() ? null : schedulers;
        });
    }

    @Override
    public void onGameEvents(GameEventBatch batch) {
        Map<UUID, AiDecisionScheduler> schedulers = schedulersByGame.get(batch.gameId());
        if (schedulers == null || schedulers.isEmpty()) {
            return;
        }

        for (GameEventEnvelope envelope : batch.events()) {
            if (envelope.fact() instanceof GameEventFact.GameEnded) {
                closeGame(batch.gameId(), schedulers);
                return;
            }

            AiDecisionKind kind = decisionKind(envelope.fact());
            if (kind == null) {
                continue;
            }
            for (Map.Entry<UUID, AiDecisionScheduler> entry : schedulers.entrySet()) {
                if (includes(envelope.audience(), entry.getKey())) {
                    entry.getValue().scheduleDecision(kind);
                }
            }
        }
    }

    private void closeGame(UUID gameId, Map<UUID, AiDecisionScheduler> schedulers) {
        schedulersByGame.remove(gameId, schedulers);
        schedulers.values().forEach(AiDecisionScheduler::close);
    }

    private static AiDecisionKind decisionKind(GameEventFact fact) {
        if (fact instanceof GameEventFact.StateInvalidated) {
            return AiDecisionKind.GAME_STATE;
        }
        if (!(fact instanceof GameEventFact.DecisionRequested decision)) {
            return null;
        }
        return switch (decision.decisionKind()) {
            case MULLIGAN -> AiDecisionKind.MULLIGAN;
            case CARDS_TO_BOTTOM -> AiDecisionKind.CARDS_TO_BOTTOM;
            case ATTACKER_DECLARATION -> AiDecisionKind.ATTACKER_DECLARATION;
            case BLOCKER_DECLARATION -> AiDecisionKind.BLOCKER_DECLARATION;
            case INTERACTION -> AiDecisionKind.INTERACTION;
            case COMBAT_DAMAGE_ASSIGNMENT -> AiDecisionKind.COMBAT_DAMAGE_ASSIGNMENT;
        };
    }

    private static boolean includes(GameEventAudience audience, UUID playerId) {
        return switch (audience.visibility()) {
            case PUBLIC -> true;
            case PRIVATE -> audience.playerIds().contains(playerId);
            case INTERNAL -> false;
        };
    }
}
