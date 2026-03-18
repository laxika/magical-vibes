package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

public record TapMultiplePermanentsCost(int count, PermanentPredicate filter, boolean excludeSource) implements CostEffect {

    public TapMultiplePermanentsCost(int count, PermanentPredicate filter) {
        this(count, filter, false);
    }
}
