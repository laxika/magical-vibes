package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.TargetFilter;

public record ReturnSourceAsAuraEffect(TargetFilter enchantFilter) implements CardEffect {
}
