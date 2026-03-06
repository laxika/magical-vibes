package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

public record RemoveCounterFromSourceCost(int count, CounterType counterType) implements CostEffect {

    public RemoveCounterFromSourceCost() {
        this(1, CounterType.ANY);
    }

    public RemoveCounterFromSourceCost(int count) {
        this(count, CounterType.ANY);
    }
}
