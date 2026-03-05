package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

public record SacrificePermanentCost(PermanentPredicate filter, String description) implements CostEffect {
}
