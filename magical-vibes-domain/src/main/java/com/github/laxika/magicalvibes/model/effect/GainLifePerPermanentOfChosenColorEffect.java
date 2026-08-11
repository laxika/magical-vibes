package com.github.laxika.magicalvibes.model.effect;

/**
 * "Choose a color, then gain 1 life for each permanent of that color." (Treva, the Renewer.)
 *
 * <p>The color is chosen during resolution and the count includes matching permanents on every
 * battlefield.</p>
 */
public record GainLifePerPermanentOfChosenColorEffect() implements CardEffect {
}
