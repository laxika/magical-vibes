package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The sum of the effective power of the creatures controlled by the amount's controller that
 * match the optional filter.
 */
public record TotalPowerOfControlledCreatures(PermanentPredicate filter) implements DynamicAmount {

    public TotalPowerOfControlledCreatures() {
        this(null);
    }
}
