package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect: if the controller would gain life, they gain twice that much instead.
 * Used by Boon Reflection. Multiple instances stack multiplicatively (two doublers = quadruple),
 * matching the Rhox Faithmender / Alhammarret's Archive ruling — the effective life gained is
 * {@code amount * 2^(number of controlled doublers)}. Applied in {@code LifeSupport.applyGainLife}.
 */
public record DoubleLifeGainEffect() implements CardEffect {
}
