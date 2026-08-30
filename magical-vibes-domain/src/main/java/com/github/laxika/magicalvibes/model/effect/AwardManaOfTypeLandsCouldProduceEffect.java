package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Adds mana of one type that a land in the selected scope could produce. The amount is chosen
 * after the type is chosen, so an amount greater than one produces that many mana of the same
 * type rather than prompting independently for each mana.
 */
public record AwardManaOfTypeLandsCouldProduceEffect(ManaColorLandScope scope,
                                                     PermanentPredicate landPredicate,
                                                     DynamicAmount amount)
        implements ManaProducingEffect {

    public AwardManaOfTypeLandsCouldProduceEffect(ManaColorLandScope scope,
                                                   PermanentPredicate landPredicate) {
        this(scope, landPredicate, new Fixed(1));
    }
}
