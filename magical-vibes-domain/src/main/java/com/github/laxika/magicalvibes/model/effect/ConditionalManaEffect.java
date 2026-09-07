package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.Condition;

import java.util.List;

/** Adds one of two colors depending on a condition evaluated when the ability resolves. */
public record ConditionalManaEffect(Condition condition, ManaColor ifMetColor,
                                    ManaColor ifNotMetColor, DynamicAmount amount)
        implements ManaProducingEffect {

    public ConditionalManaEffect(Condition condition, ManaColor ifMetColor, ManaColor ifNotMetColor) {
        this(condition, ifMetColor, ifNotMetColor, new Fixed(1));
    }

    @Override
    public DynamicAmount estimatedManaAmount() {
        return amount;
    }

    @Override
    public List<ManaColor> estimatedMutuallyExclusiveManaColors() {
        return ifMetColor == ifNotMetColor
                ? List.of(ifMetColor)
                : List.of(ifMetColor, ifNotMetColor);
    }
}
