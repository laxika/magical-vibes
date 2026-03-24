package com.github.laxika.magicalvibes.model.effect;

/**
 * Gain life equal to X × multiplier, where X is the xValue on the stack entry.
 * For example, Sanguine Sacrament uses multiplier=2 for "gain twice X life."
 */
public record GainLifeMultipliedByXValueEffect(int multiplier) implements CardEffect {
}
