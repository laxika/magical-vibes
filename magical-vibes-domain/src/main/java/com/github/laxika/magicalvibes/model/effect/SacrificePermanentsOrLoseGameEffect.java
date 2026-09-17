package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** The controller sacrifices matching permanents and loses if they cannot sacrifice the full count. */
public record SacrificePermanentsOrLoseGameEffect(DynamicAmount count, PermanentPredicate filter)
        implements CardEffect {
}
