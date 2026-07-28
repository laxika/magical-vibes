package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;

import java.util.Map;

/**
 * STATIC replacement (Reality Twist): if tapped for mana, Plains produce {R}, Swamps produce {G},
 * Mountains produce {W}, and Forests produce {B} instead of any other type. Islands are unchanged.
 * Applied in mana-ability resolution via {@code GameQueryService.twistedLandManaColors}.
 */
public record RealityTwistManaEffect() implements TwistBasicLandManaColorsEffect {

    private static final Map<CardSubtype, ManaColor> MAPPING = Map.of(
            CardSubtype.PLAINS, ManaColor.RED,
            CardSubtype.SWAMP, ManaColor.GREEN,
            CardSubtype.MOUNTAIN, ManaColor.WHITE,
            CardSubtype.FOREST, ManaColor.BLACK,
            CardSubtype.ISLAND, ManaColor.BLUE);

    @Override
    public Map<CardSubtype, ManaColor> landColorMapping() {
        return MAPPING;
    }
}
