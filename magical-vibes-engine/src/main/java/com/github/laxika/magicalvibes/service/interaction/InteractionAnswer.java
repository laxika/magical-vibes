package com.github.laxika.magicalvibes.service.interaction;

/**
 * A player's answer to a {@link com.github.laxika.magicalvibes.model.PendingInteraction},
 * as it arrives from the wire (via {@code GameService}'s answer entry points). Each record
 * mirrors one wire-message payload shape; new shapes are added as interaction kinds migrate
 * to the {@link InteractionHandlerRegistry}.
 */
public sealed interface InteractionAnswer {

    /** A single non-negative number (e.g. an X value). */
    record NumberChosen(int value) implements InteractionAnswer {
    }
}
