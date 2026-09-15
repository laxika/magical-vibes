package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** Adds restricted mana to a target player. */
public record AwardTargetedRestrictedManaEffect(ManaColor color,
                                                DynamicAmount amount,
                                                ManaRestriction restriction) implements ManaProducingEffect {

    public AwardTargetedRestrictedManaEffect(ManaColor color, int amount, ManaRestriction restriction) {
        this(color, new Fixed(amount), restriction);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
