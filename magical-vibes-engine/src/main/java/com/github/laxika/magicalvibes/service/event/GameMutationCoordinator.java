package com.github.laxika.magicalvibes.service.event;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.event.GameEventAudience;
import com.github.laxika.magicalvibes.model.event.GameEventBatch;
import com.github.laxika.magicalvibes.model.event.GameEventEnvelope;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Records an ordered event batch while mutable GameData changes under its monitor, then dispatches
 * the completed batch after releasing that monitor.
 *
 * <p>The coordinator is intentionally the only owner of mutation-scope nesting and flushing.
 * Nested calls for the same game append to the outer context and inherit its causal action id.
 */
@Component
public class GameMutationCoordinator {

    private final GameEventDispatcher dispatcher;
    private final Map<GameData, ActionState> actionStates =
            Collections.synchronizedMap(new WeakHashMap<>());
    private final Set<ActionState> activeActions = ConcurrentHashMap.newKeySet();

    public GameMutationCoordinator(GameEventDispatcher dispatcher) {
        this.dispatcher = Objects.requireNonNull(dispatcher, "dispatcher");
    }

    /**
     * Starts a new causal action with an engine-generated identity. Public runtime boundaries use
     * this overload; tests and adapters that already own a command identity may use the explicit
     * overload below.
     */
    public void mutate(GameData gameData, Runnable mutation) {
        mutate(gameData, UUID.randomUUID(), mutation);
    }

    public <T> T mutate(GameData gameData, Supplier<T> mutation) {
        return mutate(gameData, UUID.randomUUID(), mutation);
    }

    /**
     * Returns whether the current call is already inside this game's protected causal action.
     * Canonical facades use this to let public overloads and recursive engine continuations join
     * the outer action without storing scope state on a thread.
     */
    public boolean isInAction(GameData gameData) {
        Objects.requireNonNull(gameData, "gameData");
        ActionState actionState;
        synchronized (actionStates) {
            actionState = actionStates.get(gameData);
        }
        return actionState != null
                && actionState.actionLock.isHeldByCurrentThread()
                && actionState.context != null
                && Thread.holdsLock(gameData);
    }

    public void mutate(GameData gameData, UUID causalActionId, Runnable mutation) {
        mutate(gameData, causalActionId, () -> {
            mutation.run();
            return null;
        });
    }

    public <T> T mutate(GameData gameData, UUID causalActionId, Supplier<T> mutation) {
        Objects.requireNonNull(gameData, "gameData");
        Objects.requireNonNull(causalActionId, "causalActionId");
        Objects.requireNonNull(mutation, "mutation");

        ActionState actionState = actionStateFor(gameData);
        ActionState currentThreadAction = currentThreadAction();
        if (currentThreadAction != null && currentThreadAction != actionState) {
            throw new IllegalStateException("A mutation scope cannot span multiple GameData instances");
        }
        if (Thread.holdsLock(gameData)) {
            if (actionState.actionLock.isHeldByCurrentThread() && actionState.context != null) {
                return mutation.get();
            }
            throw new IllegalStateException(
                    "Start the outermost GameMutationCoordinator scope before acquiring the GameData monitor");
        }

        actionState.actionLock.lock();
        boolean registeredActiveAction = activeActions.add(actionState);
        try {
            if (actionState.dispatching) {
                throw new IllegalStateException(
                        "A subscriber cannot start a mutation while the previous action is still dispatching");
            }

            MutationContext context = new MutationContext(gameData, causalActionId);
            actionState.context = context;

            T result;
            GameEventBatch batch;
            try {
                synchronized (gameData) {
                    result = mutation.get();
                    batch = completeBatch(context);
                }
            } finally {
                actionState.context = null;
            }

            // The per-game action lock remains held so a later action cannot overtake this batch, but
            // the GameData monitor is no longer held. Subscriber failures are isolated below.
            actionState.dispatching = true;
            try {
                dispatcher.dispatch(batch);
            } finally {
                actionState.dispatching = false;
            }
            return result;
        } finally {
            if (registeredActiveAction) {
                activeActions.remove(actionState);
            }
            actionState.actionLock.unlock();
        }
    }

    /**
     * Records an internal-only fact. This overload is intentionally hidden-information-safe:
     * player visibility always requires the explicit-audience overload.
     */
    public void emit(GameData gameData, GameEventFact fact) {
        emit(gameData, fact, GameEventAudience.internalOnly());
    }

    public void emit(GameData gameData, GameEventFact fact, GameEventAudience audience) {
        Objects.requireNonNull(gameData, "gameData");
        Objects.requireNonNull(fact, "fact");
        Objects.requireNonNull(audience, "audience");

        ActionState actionState = actionStateFor(gameData);
        MutationContext context = actionState.context;
        if (context == null || context.gameData != gameData
                || !actionState.actionLock.isHeldByCurrentThread()) {
            throw new IllegalStateException("Domain events may only be emitted inside their game's mutation scope");
        }
        if (!Thread.holdsLock(gameData)) {
            throw new IllegalStateException("Domain events must be recorded while holding the GameData monitor");
        }
        if ((fact instanceof GameEventFact.PrivateReveal
                || fact instanceof GameEventFact.DecisionRequested)
                && audience.visibility() != GameEventAudience.Visibility.PRIVATE) {
            throw new IllegalArgumentException(
                    "Decisions and private reveals require an explicit private audience");
        }

        context.append(fact, audience);
    }

    private GameEventBatch completeBatch(MutationContext context) {
        GameData gameData = context.gameData;
        long stateVersion = gameData.advanceDomainStateVersion();
        List<GameEventEnvelope> envelopes = new ArrayList<>(context.pendingEvents.size());

        for (PendingEvent pending : context.pendingEvents) {
            envelopes.add(new GameEventEnvelope(
                    gameData.id,
                    gameData.nextDomainEventSequence(),
                    context.causalActionId,
                    stateVersion,
                    pending.fact.kind(),
                    pending.fact,
                    pending.audience));
        }

        GameEventBatch.DispatchMode mode = gameData.simulation
                ? GameEventBatch.DispatchMode.SUPPRESSED_SIMULATION
                : GameEventBatch.DispatchMode.LIVE;
        return new GameEventBatch(gameData.id, context.causalActionId, stateVersion, mode, envelopes);
    }

    private ActionState actionStateFor(GameData gameData) {
        synchronized (actionStates) {
            return actionStates.computeIfAbsent(gameData, ignored -> new ActionState());
        }
    }

    private ActionState currentThreadAction() {
        return activeActions.stream()
                .filter(state -> state.actionLock.isHeldByCurrentThread())
                .findFirst()
                .orElse(null);
    }

    private static final class ActionState {
        private final ReentrantLock actionLock = new ReentrantLock();
        private MutationContext context;
        private boolean dispatching;
    }

    private static final class MutationContext {
        private final GameData gameData;
        private final UUID causalActionId;
        private final List<PendingEvent> pendingEvents = new ArrayList<>();
        private final Map<GameEventAudience, Integer> invalidationIndexByAudience = new HashMap<>();

        private MutationContext(GameData gameData, UUID causalActionId) {
            this.gameData = gameData;
            this.causalActionId = causalActionId;
        }

        private void append(GameEventFact fact, GameEventAudience audience) {
            if (fact instanceof GameEventFact.StateInvalidated invalidation) {
                Integer existingIndex = invalidationIndexByAudience.get(audience);
                if (existingIndex != null) {
                    PendingEvent existing = pendingEvents.get(existingIndex);
                    GameEventFact.StateInvalidated merged =
                            ((GameEventFact.StateInvalidated) existing.fact).merge(invalidation);
                    pendingEvents.set(existingIndex, new PendingEvent(merged, audience));
                    return;
                }
                invalidationIndexByAudience.put(audience, pendingEvents.size());
            }
            pendingEvents.add(new PendingEvent(fact, audience));
        }
    }

    private record PendingEvent(GameEventFact fact, GameEventAudience audience) {
    }
}
