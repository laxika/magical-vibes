package com.github.laxika.magicalvibes.model.effect;

/**
 * Proliferate. Choose any number of permanents and/or players that have a counter,
 * then give each one additional counter of each kind that permanent or player already has.
 *
 * Note: Player proliferation (poison counters) is not yet implemented.
 * Currently only handles permanent counters (+1/+1, -1/-1, loyalty).
 */
public record ProliferateEffect() implements CardEffect {
}
