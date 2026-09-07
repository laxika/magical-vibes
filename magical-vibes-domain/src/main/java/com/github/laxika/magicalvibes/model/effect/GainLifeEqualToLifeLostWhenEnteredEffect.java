package com.github.laxika.magicalvibes.model.effect;

/**
 * Leaves-the-battlefield trigger marker: the controller gains the amount of life recorded when
 * the source permanent's matching enter-the-battlefield effect resolved.
 */
public record GainLifeEqualToLifeLostWhenEnteredEffect() implements CardEffect {
}
