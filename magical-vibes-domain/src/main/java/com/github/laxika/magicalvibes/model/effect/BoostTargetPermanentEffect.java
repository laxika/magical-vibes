package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Target permanent gets +X/+Y until end of turn. The optional filter narrows the permanent target
 * to the kind named by the card, such as a creature or Vehicle.
 */
public record BoostTargetPermanentEffect(DynamicAmount powerBoost, DynamicAmount toughnessBoost,
                                         PermanentPredicate filter) implements CreatureBoostEffect {

    public BoostTargetPermanentEffect(int powerBoost, int toughnessBoost, PermanentPredicate filter) {
        this(new Fixed(powerBoost), new Fixed(toughnessBoost), filter);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent(), filter);
    }
}
