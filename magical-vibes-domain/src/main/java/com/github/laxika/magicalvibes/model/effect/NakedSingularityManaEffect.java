package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;

import java.util.Map;

/**
 * STATIC replacement (Naked Singularity): if tapped for mana, Plains produce {R}, Islands produce
 * {G}, Swamps produce {W}, Mountains produce {U}, and Forests produce {B} instead of any other
 * type. Applied in mana-ability resolution via {@code GameQueryService.twistedLandManaColors}.
 */
public record NakedSingularityManaEffect() implements TwistBasicLandManaColorsEffect {

    private static final Map<CardSubtype, ManaColor> MAPPING = Map.of(
            CardSubtype.PLAINS, ManaColor.RED,
            CardSubtype.ISLAND, ManaColor.GREEN,
            CardSubtype.SWAMP, ManaColor.WHITE,
            CardSubtype.MOUNTAIN, ManaColor.BLUE,
            CardSubtype.FOREST, ManaColor.BLACK);

    @Override
    public Map<CardSubtype, ManaColor> landColorMapping() {
        return MAPPING;
    }
}
