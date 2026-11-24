package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Reduces this spell's casting cost by the given amount if its selected target is a permanent
 * matching the predicate. The target index is zero-based and uses -1 to consider any target. When
 * {@code controlledByCaster} is set, the selected target must also be controlled by the player
 * casting the spell ("if it targets a Dinosaur you control"). With {@code increase} set it is the surcharge "this spell
 * costs {N} more to cast if it targets a [permanent]" ({@link #increaseIfTargeting}), judged on the first target;
 * a spell may then pick its cheapest legal target as it checks playability.
 */
public record ReduceOwnCastCostIfTargetingPermanentEffect(PermanentPredicate predicate,
                                                          int amount,
                                                          boolean controlledByCaster,
                                                          int targetIndex,
                                                          boolean increase) implements CardEffect {

    public ReduceOwnCastCostIfTargetingPermanentEffect(PermanentPredicate predicate, int amount,
                                                       boolean controlledByCaster, int targetIndex) {
        this(predicate, amount, controlledByCaster, targetIndex, false);
    }

    /** "This spell costs {N} more to cast if it targets a [permanent]" (Dragon's Prey, Vanish into Eternity). */
    public static ReduceOwnCastCostIfTargetingPermanentEffect increaseIfTargeting(PermanentPredicate predicate,
                                                                                 int amount) {
        return new ReduceOwnCastCostIfTargetingPermanentEffect(predicate, amount, false, 0, true);
    }

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
