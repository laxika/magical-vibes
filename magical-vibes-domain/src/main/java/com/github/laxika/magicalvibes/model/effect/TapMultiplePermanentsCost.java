package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

public record TapMultiplePermanentsCost(int count, PermanentPredicate filter) implements CostEffect {
}
