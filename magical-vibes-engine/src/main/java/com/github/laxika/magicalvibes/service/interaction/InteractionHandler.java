package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;

import java.util.UUID;

/**
 * Validates and applies answers for one {@link PendingInteraction} kind. Most generic
 * interactions also render their projection-side prompt here; combat decisions are rendered
 * directly by the canonical event subscriber from immutable interaction snapshots.
 * Implementations are Spring beans auto-registered into the {@link InteractionHandlerRegistry}
 * by {@code GameEngineConfig} (same discovery pattern as the effect handler registries).
 *
 * <p>The deciding player and the legal answer space live on the interaction record itself
 * ({@link PendingInteraction#decidingPlayerId()} / {@link PendingInteraction#legalOptions()}),
 * not on the handler.
 */
public interface InteractionHandler<T extends PendingInteraction> {

    /** The interaction record class this handler owns. */
    Class<T> handledType();

    /** The wire-payload shape this handler accepts; other shapes fall through to legacy dispatch. */
    Class<? extends InteractionAnswer> answerType();

    /**
     * Sends the projection-side prompt for a generic interaction to {@code recipientId}. Combat
     * handlers intentionally use the default because their prompts are canonical event
     * projections.
     */
    default void prompt(GameData gameData, T interaction, UUID recipientId) {
        throw new UnsupportedOperationException(
                handledType().getSimpleName() + " is projected outside its answer handler");
    }

    /** Validates and applies the player's answer, then advances the game (queue, stack, auto-pass). */
    void handleAnswer(GameData gameData, Player player, T interaction, InteractionAnswer answer);
}
