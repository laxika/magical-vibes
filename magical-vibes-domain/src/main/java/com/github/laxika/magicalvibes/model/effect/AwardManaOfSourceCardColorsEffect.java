package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.List;

/** Adds one mana of a color in the activating permanent's current colors. */
public record AwardManaOfSourceCardColorsEffect() implements ManaProducingEffect {

    @Override
    public DynamicAmount estimatedManaAmount() {
        return new Fixed(1);
    }

    @Override
    public List<ManaColor> estimatedMutuallyExclusiveManaColors() {
        return ManaColor.COLORS;
    }
}
