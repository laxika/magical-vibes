package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the top {@code count} cards of the controller's library and lets that player play them
 * until their next end step.
 */
public record ExileTopCardsMayPlayUntilNextEndStepEffect(int count) implements CardEffect {
}
