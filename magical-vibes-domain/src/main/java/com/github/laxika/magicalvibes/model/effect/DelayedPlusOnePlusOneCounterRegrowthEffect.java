package com.github.laxika.magicalvibes.model.effect;

/**
 * "Whenever a +1/+1 counter is removed from this creature, put two +1/+1 counters on it at the beginning
 * of the next end step." (e.g. Protean Hydra)
 *
 * Used in the {@code ON_SELF_PLUS_ONE_PLUS_ONE_COUNTER_REMOVED} slot: the ability triggers once for each
 * +1/+1 counter removed, and on resolution registers a delayed trigger in the
 * DelayedPlusOneCounters delayed actions which fires at the next end step.
 */
public record DelayedPlusOnePlusOneCounterRegrowthEffect() implements CardEffect {
}
