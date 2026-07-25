package com.github.laxika.magicalvibes.service.event;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.event.GameEventAudience;
import com.github.laxika.magicalvibes.model.event.GameEventBatch;
import com.github.laxika.magicalvibes.model.event.GameEventEnvelope;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
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

    private static final int DISPATCH_STRIPE_COUNT = 64;

    private final GameEventDispatcher dispatcher;
    private final ThreadLocal<MutationContext> activeContext = new ThreadLocal<>();
    private final ReentrantLock[] dispatchStripes = new ReentrantLock[DISPATCH_STRIPE_COUNT];

    public GameMutationCoordinator(GameEventDispatcher dispatcher) {
        this.dispatcher = Objects.requireNonNull(dispatcher, "dispatcher");
        for (int i = 0; i < dispatchStripes.length; i++) {
            dispatchStripes[i] = new ReentrantLock();
        }
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

        MutationContext existing = activeContext.get();
        if (existing != null) {
            if (existing.gameData != gameData) {
                throw new IllegalStateException("A mutation scope cannot span multiple GameData instances");
            }
            return mutation.get();
        }

        if (Thread.holdsLock(gameData)) {
            throw new IllegalStateException(
                    "Start the outermost GameMutationCoordinator scope before acquiring the GameData monitor");
        }

        ReentrantLock dispatchStripe = stripeFor(gameData);
        dispatchStripe.lock();
        try {
            MutationContext context = new MutationContext(gameData, causalActionId);
            activeContext.set(context);

            T result;
            GameEventBatch batch;
            try {
                synchronized (gameData) {
                    result = mutation.get();
                    batch = completeBatch(context);
                }
            } finally {
                activeContext.remove();
            }

            // The per-game stripe remains held so a later action cannot overtake this batch, but
            // the GameData monitor is no longer held. Subscriber failures are isolated below.
            dispatcher.dispatch(batch);
            return result;
        } finally {
            dispatchStripe.unlock();
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

        MutationContext context = activeContext.get();
        if (context == null || context.gameData != gameData) {
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

    private ReentrantLock stripeFor(GameData gameData) {
        int index = (System.identityHashCode(gameData) & Integer.MAX_VALUE) % dispatchStripes.length;
        return dispatchStripes[index];
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
