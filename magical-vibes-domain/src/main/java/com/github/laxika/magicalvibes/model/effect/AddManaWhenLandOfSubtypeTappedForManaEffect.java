package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;

/**
 * Land-tap trigger: whenever any land of the given subtype is tapped for mana,
 * its controller adds one additional mana of {@code color}. Symmetric — affects
 * every player's matching lands. Used by Vernal Bloom (Forest → additional {G}).
 */
public record AddManaWhenLandOfSubtypeTappedForManaEffect(CardSubtype subtype, ManaColor color)
        implements CardEffect {
}
