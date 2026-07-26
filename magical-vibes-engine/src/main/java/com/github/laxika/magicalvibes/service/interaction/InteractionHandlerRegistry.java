package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.event.GameEventAudience;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.service.event.GameMutationCoordinator;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Dispatches {@link PendingInteraction} kinds to their {@link InteractionHandler}s, keyed by
 * interaction record class. Created and populated by {@code GameEngineConfig} (same pattern
 * as {@code EffectHandlerRegistry}), so handler beans never create dependency cycles with
 * the services that dispatch through the registry.
 *
 */
public class InteractionHandlerRegistry {

    private final Map<Class<? extends PendingInteraction>, InteractionHandler<?>> handlers = new LinkedHashMap<>();
    private final Supplier<GameMutationCoordinator> mutationCoordinatorSupplier;

    /** Test-only state/answer-routing constructor. It deliberately performs no delivery. */
    public InteractionHandlerRegistry() {
        this.mutationCoordinatorSupplier = null;
    }

    public InteractionHandlerRegistry(Supplier<GameMutationCoordinator> mutationCoordinatorSupplier) {
        this.mutationCoordinatorSupplier = mutationCoordinatorSupplier;
    }

    public void register(InteractionHandler<?> handler) {
        handlers.put(handler.handledType(), handler);
    }

    public int size() {
        return handlers.size();
    }

    @SuppressWarnings("unchecked")
    private InteractionHandler<PendingInteraction> handlerFor(PendingInteraction interaction) {
        return (InteractionHandler<PendingInteraction>) handlers.get(interaction.getClass());
    }

    /**
     * Marks the interaction as active and prompts the deciding player (with mind-control
     * recipient redirection, matching the legacy {@code PlayerInputService} begin methods).
     */
    public void begin(GameData gameData, PendingInteraction interaction) {
        InteractionHandler<PendingInteraction> handler = handlerFor(interaction);
        if (handler == null) {
            throw new IllegalArgumentException("No interaction handler registered for " + interaction.getClass().getName());
        }
        gameData.interaction.beginInteraction(interaction);
        emitActiveDecision(gameData);
    }

    /**
     * Marks the interaction active without prompting, for begin sites that must interleave
     * other sends (e.g. a game-state broadcast) between the state change and the prompt.
     * Follow with {@link #promptActive}.
     */
    public void beginWithoutPrompt(GameData gameData, PendingInteraction interaction) {
        InteractionHandler<PendingInteraction> handler = handlerFor(interaction);
        if (handler == null) {
            throw new IllegalArgumentException("No interaction handler registered for " + interaction.getClass().getName());
        }
        gameData.interaction.beginInteraction(interaction);
    }

    /** Prompts the active interaction's deciding player (mind-control recipient resolved). */
    public void promptActive(GameData gameData) {
        PendingInteraction active = gameData.interaction.activeInteraction();
        if (active == null) {
            return;
        }
        InteractionHandler<PendingInteraction> handler = handlerFor(active);
        if (handler == null) {
            return;
        }
        emitActiveDecision(gameData);
    }

    /**
     * Re-delivers the currently active logical decision after an invalid answer without allocating
     * a new decision identity.
     */
    public void requestActiveDecision(GameData gameData) {
        PendingInteraction active = gameData.interaction.activeInteraction();
        if (active == null) {
            return;
        }
        InteractionHandler<PendingInteraction> handler = handlerFor(active);
        if (handler != null) {
            emitActiveDecision(gameData);
        }
    }

    private void emitActiveDecision(GameData gameData) {
        PendingInteraction active = gameData.interaction.activeInteraction();
        UUID decider = active.decidingPlayerId();
        UUID recipient = resolveMessageRecipient(gameData, decider);
        if (mutationCoordinatorSupplier == null) {
            return;
        }
        mutationCoordinatorSupplier.get().emit(gameData,
                new GameEventFact.DecisionRequested(
                        gameData.interaction.activeDecisionId(),
                        decider,
                        decisionKind(active)),
                GameEventAudience.player(recipient));
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

    /**
     * Routes a wire answer to the active interaction's handler. Returns {@code false} when no
     * registry-managed interaction is active or the answer shape does not match — the caller
     * then continues down the legacy dispatch path (which supplies the legacy error message).
     */
    public boolean dispatchAnswer(GameData gameData, Player player, InteractionAnswer answer) {
        PendingInteraction active = gameData.interaction.activeInteraction();
        if (active == null) {
            return false;
        }
        InteractionHandler<PendingInteraction> handler = handlerFor(active);
        if (handler == null || !handler.answerType().isInstance(answer)) {
            return false;
        }
        handler.handleAnswer(gameData, player, active, answer);
        return true;
    }

    /**
     * Reports whether the active interaction is registry-managed so legacy replay dispatch can
     * skip it. Reconnect prompt delivery is an explicit {@code DecisionRequested} replay fact.
     */
    public boolean replayPrompt(GameData gameData, UUID reconnectingPlayerId) {
        PendingInteraction active = gameData.interaction.activeInteraction();
        if (active == null) {
            return false;
        }
        InteractionHandler<PendingInteraction> handler = handlerFor(active);
        if (handler == null) {
            return false;
        }
        return true;
    }

    /**
     * The deciding player of the active registry-managed interaction, or {@code null} when
     * none is active (used by the mind-control acting-player resolution in {@code GameService}).
     */
    public UUID activeDecidingPlayerId(GameData gameData) {
        PendingInteraction active = gameData.interaction.activeInteraction();
        return active != null ? active.decidingPlayerId() : null;
    }

    /**
     * When mind control is active, redirect messages intended for the controlled player
     * to the controlling player instead (mirrors {@code PlayerInputService}).
     */
    private static UUID resolveMessageRecipient(GameData gameData, UUID playerId) {
        if (gameData.mindControlledPlayerId != null
                && gameData.mindControlledPlayerId.equals(playerId)
                && gameData.mindControllerPlayerId != null) {
            return gameData.mindControllerPlayerId;
        }
        return playerId;
    }

    /** Exact interaction classes registered for answer handling. */
    public java.util.Set<Class<? extends PendingInteraction>> registeredTypes() {
        return java.util.Set.copyOf(handlers.keySet());
    }
}
