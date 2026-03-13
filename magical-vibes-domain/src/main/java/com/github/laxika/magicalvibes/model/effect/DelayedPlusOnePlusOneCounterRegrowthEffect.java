package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: "Whenever a +1/+1 counter is removed from this creature,
 * put two +1/+1 counters on it at the beginning of the next end step."
 * (e.g. Protean Hydra)
 *
 * When +1/+1 counters are removed, this registers a delayed trigger in
 * GameData.pendingDelayedPlusOnePlusOneCounters which fires at the next end step.
 */
public record DelayedPlusOnePlusOneCounterRegrowthEffect() implements CardEffect {
}
