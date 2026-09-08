package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The greatest effective power among creatures the controller controls that match the optional
 * filter.
 */
public record GreatestPowerAmongControlled(PermanentPredicate filter, boolean floorAtZero) implements DynamicAmount {

    public GreatestPowerAmongControlled(PermanentPredicate filter) {
        this(filter, true);
    }

    public GreatestPowerAmongControlled() {
        this(null, true);
    }

    public static GreatestPowerAmongControlled includingNegative(PermanentPredicate filter) {
        return new GreatestPowerAmongControlled(filter, false);
    }
}
