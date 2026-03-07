package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: the controller of this permanent doesn't lose the game for having 0 or less life.
 * Unlike {@link CantLoseGameEffect}, this does NOT prevent losing from poison counters or other effects.
 */
public record CantLoseGameFromLifeEffect() implements CardEffect {
}
