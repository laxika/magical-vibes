package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Puts a counter of {@code counterType} on the creature enchanted by the source Aura. If that creature
 * then has {@code threshold} or more counters of that type on it, it deals damage equal to its power to
 * its controller and is then destroyed and can't be regenerated.
 *
 * <p>Both halves live in one effect because they are a single triggered ability (Consuming Ferocity's
 * upkeep trigger); splitting them would put two independent abilities on the stack.
 */
public record AddCounterToEnchantedCreatureThenDestroyAtThresholdEffect(CounterType counterType, int threshold)
        implements CardEffect {
}
