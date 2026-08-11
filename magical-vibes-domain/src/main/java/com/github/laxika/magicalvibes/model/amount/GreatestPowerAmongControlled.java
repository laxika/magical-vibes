package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The greatest effective power among creatures the controller controls that match the optional
 * filter (0 when no creature matches; negative powers never lower the result below 0).
 */
public record GreatestPowerAmongControlled(PermanentPredicate filter) implements DynamicAmount {

    public GreatestPowerAmongControlled() {
        this(null);
    }
}
