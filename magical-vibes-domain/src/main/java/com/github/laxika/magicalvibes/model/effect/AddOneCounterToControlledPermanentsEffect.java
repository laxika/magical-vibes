package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/** Doc Samson-style replacement for counters put on permanents its controller controls. */
public record AddOneCounterToControlledPermanentsEffect() implements CounterReplacementEffect {

    @Override
    public int replace(CounterType counterType, int count) {
        return count > 0 ? count + 1 : count;
    }
}
