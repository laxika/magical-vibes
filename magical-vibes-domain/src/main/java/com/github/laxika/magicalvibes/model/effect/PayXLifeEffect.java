package com.github.laxika.magicalvibes.model.effect;

/**
 * Pay any amount of life and record the amount on the resolving stack entry for a following
 * effect. The amount is chosen during resolution and is capped at the controller's life total.
 */
public record PayXLifeEffect() implements CardEffect {
}
