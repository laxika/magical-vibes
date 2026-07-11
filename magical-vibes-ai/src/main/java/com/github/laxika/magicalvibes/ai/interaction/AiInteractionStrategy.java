package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;

/**
 * Computes and sends the AI's answer for one {@link PendingInteraction} kind. One strategy
 * per interaction record class, looked up via {@link AiInteractionStrategies} — the AI-side
 * counterpart of the engine's {@code InteractionHandlerRegistry}. Strategies answer through
 * the same wire-shaped {@code AiGameActions} calls a human client would send.
 */
public interface AiInteractionStrategy<T extends PendingInteraction> {

    /** The interaction record class this strategy answers. */
    Class<T> handledType();

    /** Computes and sends the answer. No-ops when the AI is not the deciding player. */
    void answer(T interaction, AiInteractionContext ctx) throws Exception;
}
