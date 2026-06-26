package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

public record SacrificePermanentCost(PermanentPredicate filter, String description, boolean excludeSource) implements CostEffect {
    public SacrificePermanentCost(PermanentPredicate filter, String description) {
        this(filter, description, true);
    }
}
