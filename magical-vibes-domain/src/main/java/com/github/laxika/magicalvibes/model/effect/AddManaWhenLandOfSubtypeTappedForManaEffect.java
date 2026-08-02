package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;

/**
 * Land-tap trigger: whenever a land of the given subtype is tapped for mana,
 * its controller adds one additional mana of {@code color}. The optional
 * controller-only form watches only lands tapped by the source's controller.
 */
public record AddManaWhenLandOfSubtypeTappedForManaEffect(
        CardSubtype subtype,
        ManaColor color,
        boolean controllerOnly
) implements CardEffect {

    public AddManaWhenLandOfSubtypeTappedForManaEffect(CardSubtype subtype, ManaColor color) {
        this(subtype, color, false);
    }
}
