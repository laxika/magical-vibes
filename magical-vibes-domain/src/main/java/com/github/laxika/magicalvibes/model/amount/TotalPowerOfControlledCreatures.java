package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The sum of the effective power of the creatures controlled by the amount's controller that
 * match the optional filter.
 *
 * <p>With {@code declaredAttackersOnly} the sum is taken over the creatures recorded as declared
 * attackers on the resolving triggered ability instead of the battlefield ("their total power"):
 * a creature that has since left combat still counts, and one that has left the battlefield counts
 * with its last known power. Such an amount only has a value while the ability resolves, so it
 * never limits which targets can be chosen.</p>
 */
public record TotalPowerOfControlledCreatures(PermanentPredicate filter, boolean declaredAttackersOnly)
        implements DynamicAmount {

    public TotalPowerOfControlledCreatures(PermanentPredicate filter) {
        this(filter, false);
    }

    public TotalPowerOfControlledCreatures() {
        this(null, false);
    }

    /** Total power of the declared attackers matching {@code filter}, read as the ability resolves. */
    public static TotalPowerOfControlledCreatures declaredAttackers(PermanentPredicate filter) {
        return new TotalPowerOfControlledCreatures(filter, true);
    }
}
