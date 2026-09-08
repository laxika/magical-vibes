package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/** Puts one counter of the specified type on each permanent that convoked the resolving spell. */
public record PutCounterOnConvokeCreaturesEffect(CounterType counterType) implements CardEffect {
}
