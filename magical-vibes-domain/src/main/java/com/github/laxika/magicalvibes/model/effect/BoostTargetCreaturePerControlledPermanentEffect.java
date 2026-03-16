package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Boosts target creature by +N/+N per controlled permanent matching the filter.
 * For example, Elder of Laurels uses (1, 1, PermanentIsCreaturePredicate) for
 * "+X/+X where X is the number of creatures you control".
 */
public record BoostTargetCreaturePerControlledPermanentEffect(
        int powerPerPermanent,
        int toughnessPerPermanent,
        PermanentPredicate filter
) implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
