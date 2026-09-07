package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;

/** Adds one mana whenever a land is tapped for the specified fixed mana color. */
public record AddManaWhenLandOfColorTappedForManaEffect(ManaColor color, boolean controllerOnly)
        implements CardEffect {

    /** Watches only lands tapped by the source controller. */
    public AddManaWhenLandOfColorTappedForManaEffect(ManaColor color) {
        this(color, true);
    }
}
