package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.List;

/** Adds mana of a separately chosen color from a fixed list with a spending restriction. */
public record AwardRestrictedManaOfColorsEffect(List<ManaColor> colors, DynamicAmount amount,
                                                ManaRestriction restriction)
        implements ManaProducingEffect {

    public AwardRestrictedManaOfColorsEffect(List<ManaColor> colors, int amount,
                                             ManaRestriction restriction) {
        this(colors, new Fixed(amount), restriction);
    }
}
