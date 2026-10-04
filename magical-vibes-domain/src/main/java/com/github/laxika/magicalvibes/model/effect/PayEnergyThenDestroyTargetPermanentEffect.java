package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

/** Pays dynamic energy for a target nonland permanent, then queues non-targeting reflexive destruction. */
public record PayEnergyThenDestroyTargetPermanentEffect(DynamicAmount energyAmount)
        implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanents(
                new PermanentNotPredicate(new PermanentIsLandPredicate())));
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.DESTROY;
    }
}
