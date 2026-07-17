package com.github.laxika.magicalvibes.model.effect;

/**
 * Schedule the source permanent to be exiled at the beginning of the next end step
 * (e.g. Dark Maze's "Exile it at the beginning of the next end step" on its own attack ability).
 * Operates on the source, so it carries no target.
 */
public record ExileSelfAtEndStepEffect() implements CardEffect {
}
