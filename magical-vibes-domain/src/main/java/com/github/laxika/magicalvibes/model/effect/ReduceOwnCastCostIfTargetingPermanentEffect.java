package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Reduces this spell's casting cost by the given amount if any chosen target is a permanent
 * matching the predicate. When {@code controlledByCaster} is set, the matching target must also
 * be controlled by the player casting the spell ("if it targets a Dinosaur you control").
 */
public record ReduceOwnCastCostIfTargetingPermanentEffect(PermanentPredicate predicate,
                                                          int amount,
                                                          boolean controlledByCaster) implements CardEffect {

    /** Convenience for the plain "if it targets a matching permanent" form (any controller). */
    public ReduceOwnCastCostIfTargetingPermanentEffect(PermanentPredicate predicate, int amount) {
        this(predicate, amount, false);
    }
}
