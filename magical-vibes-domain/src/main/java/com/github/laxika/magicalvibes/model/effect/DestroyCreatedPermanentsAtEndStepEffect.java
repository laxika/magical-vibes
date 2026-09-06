package com.github.laxika.magicalvibes.model.effect;

/**
 * Schedule every permanent created earlier in this same resolution for destruction at the
 * beginning of the next end step.
 */
public record DestroyCreatedPermanentsAtEndStepEffect() implements CardEffect {
}
