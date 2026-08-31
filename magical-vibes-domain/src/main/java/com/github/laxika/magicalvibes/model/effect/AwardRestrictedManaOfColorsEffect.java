package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.List;

/** Adds mana of one color chosen from a fixed list, with a spend restriction. */
public record AwardRestrictedManaOfColorsEffect(List<ManaColor> colors, DynamicAmount amount,
                                                ManaRestriction restriction)
        implements ManaProducingEffect {

    public AwardRestrictedManaOfColorsEffect(List<ManaColor> colors, ManaRestriction restriction) {
        this(colors, new Fixed(1), restriction);
    }

    public AwardRestrictedManaOfColorsEffect {
        colors = List.copyOf(colors);
    }
}
