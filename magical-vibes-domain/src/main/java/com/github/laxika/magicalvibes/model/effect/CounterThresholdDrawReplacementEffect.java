package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/** A draw replacement that is active only while its source has enough counters. */
public interface CounterThresholdDrawReplacementEffect extends CardEffect {

    CounterType counterType();

    int minimumCounters();
}
