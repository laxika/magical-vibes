package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;

/** Adds one fixed-color mana whenever a land is tapped for mana. */
public record AddManaWhenLandTappedForManaEffect(ManaColor color, boolean controllerOnly)
        implements CardEffect {

    /** Watches only lands tapped by the source controller. */
    public AddManaWhenLandTappedForManaEffect(ManaColor color) {
        this(color, true);
    }
}
