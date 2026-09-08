package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Static replacement effect that replaces damage to this permanent's controller with that many
 * counters of the configured type on this permanent.
 */
public record DamageToControllerCounterReplacementEffect(CounterType counterType) implements CardEffect {
}
