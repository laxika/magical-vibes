package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

/** Pays a dynamic amount of energy and, if payment succeeds, destroys the target nonland permanent. */
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
