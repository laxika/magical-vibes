package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;

/**
 * Validates and applies answers for one {@link PendingInteraction} kind. Prompt production is
 * owned by the event projection layer, never by an answer handler.
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

    /** The answer-payload shape this handler accepts; other shapes reach command error handling. */
    Class<? extends InteractionAnswer> answerType();

    /**
     * Whether this handler applies {@code answer} to the active {@code interaction}. Defaults to the
     * handler's {@link #answerType()}; a handler overrides it to take a second answer shape that only
     * some contexts of its interaction understand.
     */
    default boolean acceptsAnswer(T interaction, InteractionAnswer answer) {
        return answerType().isInstance(answer);
    }

    /** Validates and applies the player's answer, then advances the game (queue, stack, auto-pass). */
    void handleAnswer(GameData gameData, Player player, T interaction, InteractionAnswer answer);
}
