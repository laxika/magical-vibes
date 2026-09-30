package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Performs populate the evaluated number of times.
 *
 * <p>Each repetition is a separate populate effect, so the controller makes a fresh
 * resolution-time choice for every repetition and newly created tokens are available to later
 * repetitions.
 */
public record PopulateNTimesEffect(DynamicAmount times) implements CardEffect {
}
