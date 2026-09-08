package com.github.laxika.magicalvibes.model.effect;

/**
 * Static marker: the permanent can't have an Equipment attached to it. The attachment legality
 * queries read this marker from the permanent itself or from effects granted to it.
 */
public record CantBeEquippedEffect() implements CardEffect {
}
