package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/** A static ability that prevents a permanent from having more than the specified count of a counter type. */
public record CounterLimitEffect(CounterType counterType, int maximum) implements CardEffect {
}
