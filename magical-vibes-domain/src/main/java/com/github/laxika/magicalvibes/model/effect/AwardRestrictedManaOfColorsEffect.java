package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.List;

/** Adds mana of a separately chosen color from a fixed list with a spending restriction. */
public record AwardRestrictedManaOfColorsEffect(List<ManaColor> colors, DynamicAmount amount,
                                                ManaRestriction restriction, boolean sameColor)
        implements ManaProducingEffect {

    public AwardRestrictedManaOfColorsEffect(List<ManaColor> colors, int amount,
                                             ManaRestriction restriction) {
        this(colors, new Fixed(amount), restriction);
    }

    public AwardRestrictedManaOfColorsEffect(List<ManaColor> colors, ManaRestriction restriction) {
        this(colors, new Fixed(1), restriction, false);
    }

    public AwardRestrictedManaOfColorsEffect(List<ManaColor> colors, DynamicAmount amount,
                                             ManaRestriction restriction) {
        this(colors, amount, restriction, false);
    }

    public AwardRestrictedManaOfColorsEffect(List<ManaColor> colors, int amount,
                                             ManaRestriction restriction, boolean sameColor) {
        this(colors, new Fixed(amount), restriction, sameColor);
    }

    public AwardRestrictedManaOfColorsEffect {
        colors = List.copyOf(colors);
    }
}
