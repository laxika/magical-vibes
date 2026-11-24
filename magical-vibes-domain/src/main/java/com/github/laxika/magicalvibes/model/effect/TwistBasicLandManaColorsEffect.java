package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;

import java.util.EnumMap;
import java.util.Map;

/**
 * STATIC replacement: "if tapped for mana, Plains produce {R}, Swamps produce {G}, … instead of any other type"
 * (Reality Twist, Naked Singularity). One replacement of the mana production event (CR 614.1a, 106.12b) for every
 * land with a listed basic land type; a land with only unlisted types is unaffected. A land with two basic land
 * types produces either type's color (an unlisted type keeps its own), all its mana of one color (the cards'
 * 2013-04-15 ruling).
 *
 * <p>Read by {@code StaticLandManaReplacements} and {@code GameQueryService.twistedLandManaColors}.
 *
 * @param landColorMapping basic land type to the color that type produces instead
 */
public record TwistBasicLandManaColorsEffect(Map<CardSubtype, ManaColor> landColorMapping) implements CardEffect {

    public TwistBasicLandManaColorsEffect {
        landColorMapping = Map.copyOf(new EnumMap<>(landColorMapping));
    }
}
