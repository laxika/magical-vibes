package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.event.GameEventAudience;
import com.github.laxika.magicalvibes.model.event.GameEventBatch;
import com.github.laxika.magicalvibes.model.event.GameEventEnvelope;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.service.event.GameEventDispatcher;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class AiDecisionEventSubscriberTest {

    private final UUID gameId = UUID.randomUUID();
    private final UUID actionId = UUID.randomUUID();
    private final UUID aiPlayerId = UUID.randomUUID();
    private final UUID otherPlayerId = UUID.randomUUID();

    @Test
    void schedulesEveryDecisionShapeDirectlyFromCanonicalFactsInOrder() {
        AiDecisionEventSubscriber subscriber = new AiDecisionEventSubscriber();
        AiDecisionScheduler connection = mock(AiDecisionScheduler.class);
        subscriber.register(gameId, aiPlayerId, connection);

        subscriber.onGameEvents(batch(GameEventBatch.DispatchMode.LIVE, List.of(
                event(new GameEventFact.StateInvalidated(
                                GameEventFact.StateSection.TURN_AND_PRIORITY),
                        GameEventAudience.allPlayers(), 1),
                decision(GameEventFact.DecisionKind.MULLIGAN, aiPlayerId, aiPlayerId, 2),
                decision(GameEventFact.DecisionKind.CARDS_TO_BOTTOM, aiPlayerId, aiPlayerId, 3),
                decision(GameEventFact.DecisionKind.ATTACKER_DECLARATION,
                        aiPlayerId, aiPlayerId, 4),
                decision(GameEventFact.DecisionKind.BLOCKER_DECLARATION,
                        aiPlayerId, aiPlayerId, 5),
                decision(GameEventFact.DecisionKind.COMBAT_DAMAGE_ASSIGNMENT,
                        aiPlayerId, aiPlayerId, 6),
                decision(GameEventFact.DecisionKind.INTERACTION, aiPlayerId, aiPlayerId, 7))));

        InOrder ordered = inOrder(connection);
        ordered.verify(connection).scheduleDecision(AiDecisionKind.GAME_STATE);
        ordered.verify(connection).scheduleDecision(AiDecisionKind.MULLIGAN);
        ordered.verify(connection).scheduleDecision(AiDecisionKind.CARDS_TO_BOTTOM);
        ordered.verify(connection).scheduleDecision(AiDecisionKind.ATTACKER_DECLARATION);
        ordered.verify(connection).scheduleDecision(AiDecisionKind.BLOCKER_DECLARATION);
        ordered.verify(connection).scheduleDecision(AiDecisionKind.COMBAT_DAMAGE_ASSIGNMENT);
        ordered.verify(connection).scheduleDecision(AiDecisionKind.INTERACTION);
    }

    @Test
    void usesEventAudienceAsDecisionOwnershipIncludingMindControl() {
        AiDecisionEventSubscriber subscriber = new AiDecisionEventSubscriber();
        AiDecisionScheduler connection = mock(AiDecisionScheduler.class);
        subscriber.register(gameId, aiPlayerId, connection);

        subscriber.onGameEvents(batch(GameEventBatch.DispatchMode.LIVE, List.of(
                decision(
                        GameEventFact.DecisionKind.INTERACTION,
                        otherPlayerId,
                        aiPlayerId,
                        1))));

        verify(connection).scheduleDecision(AiDecisionKind.INTERACTION);
    }

    @Test
    void doesNotWakeAiOutsideTheExplicitAudience() {
        AiDecisionEventSubscriber subscriber = new AiDecisionEventSubscriber();
        AiDecisionScheduler connection = mock(AiDecisionScheduler.class);
        subscriber.register(gameId, aiPlayerId, connection);

        subscriber.onGameEvents(batch(GameEventBatch.DispatchMode.LIVE, List.of(
                decision(
                        GameEventFact.DecisionKind.INTERACTION,
                        aiPlayerId,
                        otherPlayerId,
                        1))));

        verify(connection, never()).scheduleDecision(AiDecisionKind.INTERACTION);
    }

    @Test
    void gameEndClosesAndUnregistersEveryAiForTheGame() {
        AiDecisionEventSubscriber subscriber = new AiDecisionEventSubscriber();
        AiDecisionScheduler first = mock(AiDecisionScheduler.class);
        AiDecisionScheduler second = mock(AiDecisionScheduler.class);
        subscriber.register(gameId, aiPlayerId, first);
        subscriber.register(gameId, otherPlayerId, second);

        subscriber.onGameEvents(batch(GameEventBatch.DispatchMode.LIVE, List.of(
                event(
                        new GameEventFact.GameEnded(GameEventFact.GameResult.DRAW, null),
                        GameEventAudience.allPlayers(),
                        1))));
        subscriber.onGameEvents(batch(GameEventBatch.DispatchMode.LIVE, List.of(
                event(
                        new GameEventFact.StateInvalidated(
                                GameEventFact.StateSection.TURN_AND_PRIORITY),
                        GameEventAudience.allPlayers(),
                        1))));

        verify(first).close();
        verify(second).close();
        verify(first, never()).scheduleDecision(AiDecisionKind.GAME_STATE);
        verify(second, never()).scheduleDecision(AiDecisionKind.GAME_STATE);
    }

    @Test
    void suppressedSimulationBatchNeverReachesAiSubscriber() {
        AiDecisionEventSubscriber subscriber = new AiDecisionEventSubscriber();
        AiDecisionScheduler connection = mock(AiDecisionScheduler.class);
        subscriber.register(gameId, aiPlayerId, connection);
        GameEventDispatcher dispatcher = new GameEventDispatcher(List.of(subscriber));

        dispatcher.dispatch(batch(GameEventBatch.DispatchMode.SUPPRESSED_SIMULATION, List.of(
                event(
                        new GameEventFact.StateInvalidated(
                                GameEventFact.StateSection.TURN_AND_PRIORITY),
                        GameEventAudience.allPlayers(),
                        1))));

        verify(connection, never()).scheduleDecision(AiDecisionKind.GAME_STATE);
    }

    private GameEventEnvelope decision(
            GameEventFact.DecisionKind kind,
            UUID decidingPlayerId,
            UUID recipientId,
            long sequence) {
        return event(
                new GameEventFact.DecisionRequested(
                        UUID.randomUUID(), decidingPlayerId, kind),
                GameEventAudience.player(recipientId),
                sequence);
    }

    private GameEventEnvelope event(
            GameEventFact fact,
            GameEventAudience audience,
            long sequence) {
        return new GameEventEnvelope(
                gameId,
                sequence,
                actionId,
                1,
                fact.kind(),
                fact,
                audience);
    }

    private GameEventBatch batch(
            GameEventBatch.DispatchMode dispatchMode,
            List<GameEventEnvelope> events) {
        return new GameEventBatch(gameId, actionId, 1, dispatchMode, new ArrayList<>(events));
    }
}
