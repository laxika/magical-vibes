package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.List;

/**
 * Land-tap trigger: whenever any player taps a land whose subtypes include at least one of
 * {@code subtypes} for mana, that player adds one additional mana of a type the land produced.
 * Symmetric — fires for every player's matching lands. Triggers only once per land tap even
 * when the land carries several of the listed subtypes. Used by Keeper of Progenitus
 * (Mountain, Forest, or Plains).
 *
 * <p>Like {@link AddOneOfEachManaTypeProducedByLandEffect}, when the land produces multiple
 * types this adds one mana of the first type it produces.</p>
 */
public record AddProducedManaWhenLandOfSubtypeTappedEffect(List<CardSubtype> subtypes)
        implements CardEffect {
}
