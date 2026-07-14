package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

public record UntapMultiplePermanentsCost(int count, PermanentPredicate filter, boolean excludeSource) implements CostEffect {

    public UntapMultiplePermanentsCost(int count, PermanentPredicate filter) {
        this(count, filter, false);
    }
}
