package com.github.laxika.magicalvibes.model;

/**
 * A life payment component of a casting cost (e.g. 6 life for Demon of Death's Gate).
 */
public record LifeCastingCost(int amount) implements CastingCost {
}
