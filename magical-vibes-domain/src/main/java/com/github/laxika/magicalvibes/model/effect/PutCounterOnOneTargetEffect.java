package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Puts one counter on one of the current stack entry's targets, choosing among the surviving
 * targets while the effect resolves. This is a companion effect for a preceding effect that owns
 * the target group, such as "tap up to three target creatures".
 */
public record PutCounterOnOneTargetEffect(CounterType counterType) implements CardEffect {
}
