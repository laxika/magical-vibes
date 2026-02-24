package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * P/T = number of permanents you control matching the given predicate (static, characteristic-defining).
 */
public record PowerToughnessEqualToControlledPermanentCountEffect(PermanentPredicate filter) implements CardEffect {
}
