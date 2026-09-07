package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Reduces this spell's casting cost by the given amount if its selected target is a permanent
 * matching the predicate. The target index is zero-based and uses -1 to consider any target. When
 * {@code controlledByCaster} is set, the selected target must also be controlled by the player
 * casting the spell ("if it targets a Dinosaur you control").
 */
public record ReduceOwnCastCostIfTargetingPermanentEffect(PermanentPredicate predicate,
                                                          int amount,
                                                          boolean controlledByCaster,
                                                          int targetIndex) implements CardEffect {

    /** Considers every chosen target. */
    public ReduceOwnCastCostIfTargetingPermanentEffect(PermanentPredicate predicate,
                                                        int amount,
                                                        boolean controlledByCaster) {
        this(predicate, amount, controlledByCaster, -1);
    }

    /** Convenience for the plain "if it targets a matching permanent" form (any controller). */
    public ReduceOwnCastCostIfTargetingPermanentEffect(PermanentPredicate predicate, int amount) {
        this(predicate, amount, false, -1);
    }
}
