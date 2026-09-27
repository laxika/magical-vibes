package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/** Each player chooses a land they control and puts a counter on it. */
public record EachPlayerChoosesLandAndPutCounterEffect(CounterType counterType)
        implements CardEffect {
}
