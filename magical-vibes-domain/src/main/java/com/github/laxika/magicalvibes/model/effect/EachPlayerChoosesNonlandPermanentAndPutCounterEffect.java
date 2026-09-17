package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/** Each player chooses a nonland permanent they control and puts a counter on it. */
public record EachPlayerChoosesNonlandPermanentAndPutCounterEffect(CounterType counterType)
        implements CardEffect {
}
