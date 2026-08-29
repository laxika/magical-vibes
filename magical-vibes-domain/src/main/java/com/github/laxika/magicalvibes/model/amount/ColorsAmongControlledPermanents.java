package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The number of distinct effective colors among permanents controlled by the amount's controller.
 * When {@code filter} is non-null, only matching permanents contribute colors.
 */
public record ColorsAmongControlledPermanents(PermanentPredicate filter) implements DynamicAmount {

    /** Counts colors among all controlled permanents. */
    public ColorsAmongControlledPermanents() {
        this(null);
    }
}
