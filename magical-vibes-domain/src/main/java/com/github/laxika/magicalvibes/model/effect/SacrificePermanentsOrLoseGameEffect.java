package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Forces the controller to sacrifice exactly {@code count} matching permanents when possible;
 * if fewer are available, all matching permanents are sacrificed and the controller loses.
 */
public record SacrificePermanentsOrLoseGameEffect(DynamicAmount count, PermanentPredicate filter)
        implements CardEffect {

    public SacrificePermanentsOrLoseGameEffect(int count, PermanentPredicate filter) {
        this(new Fixed(count), filter);
    }
}
