package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Sacrifice exactly X matching permanents as a casting cost, where X is the value announced for
 * the spell. Used by alternate costs such as Firecat Blitz's "sacrifice X Mountains".
 */
public record SacrificeXPermanentsCastingCost(PermanentPredicate filter) implements CastingCost {
}
