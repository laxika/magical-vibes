package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The greatest effective toughness among creatures the controller controls that match the
 * optional filter, or zero when they control no matching creatures.
 */
public record GreatestToughnessAmongControlled(PermanentPredicate filter) implements DynamicAmount {

    public GreatestToughnessAmongControlled() {
        this(null);
    }
}
