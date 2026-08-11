package com.github.laxika.magicalvibes.model.effect;

/**
 * Delaying Shield's upkeep trigger. It removes all delay counters from the source, then creates one
 * pay-{1}{W}-or-lose-1-life decision for each counter removed.
 */
public record DelayingShieldUpkeepEffect() implements CardEffect {
}
