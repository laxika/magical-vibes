package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

public record SacrificeMultiplePermanentsCost(int count, PermanentPredicate filter) implements CardEffect {
}
