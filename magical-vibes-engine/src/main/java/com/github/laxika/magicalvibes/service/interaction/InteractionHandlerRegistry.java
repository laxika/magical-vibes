package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Dispatches {@link PendingInteraction} kinds to their {@link InteractionHandler}s, keyed by
 * interaction record class. Created and populated by {@code GameEngineConfig} (same pattern
 * as {@code EffectHandlerRegistry}), so handler beans never create dependency cycles with
 * the services that dispatch through the registry.
 *
 */
public class InteractionHandlerRegistry {

    private final Map<Class<? extends PendingInteraction>, InteractionHandler<?>> handlers = new LinkedHashMap<>();

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
        UUID decider = handler.decidingPlayerId(interaction);
        handler.prompt(gameData, interaction, resolveMessageRecipient(gameData, decider));
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
        UUID decider = handler.decidingPlayerId(active);
        handler.prompt(gameData, active, resolveMessageRecipient(gameData, decider));
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
     * Re-sends the active interaction's prompt to a reconnecting player. Returns {@code true}
     * when the active interaction is registry-managed (whether or not the reconnecting player
     * is the decider), so the caller skips the legacy replay switch.
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
        if (reconnectingPlayerId.equals(handler.decidingPlayerId(active))) {
            handler.prompt(gameData, active, reconnectingPlayerId);
        }
        return true;
    }

    /**
     * The deciding player of the active registry-managed interaction, or {@code null} when
     * none is active (used by the mind-control acting-player resolution in {@code GameService}).
     */
    public UUID activeDecidingPlayerId(GameData gameData) {
        PendingInteraction active = gameData.interaction.activeInteraction();
        if (active == null) {
            return null;
        }
        InteractionHandler<PendingInteraction> handler = handlerFor(active);
        return handler != null ? handler.decidingPlayerId(active) : null;
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
}
