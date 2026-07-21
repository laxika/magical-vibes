package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * A return-to-owner's-hand component of a casting cost (e.g. the Alara Reborn Borderposts:
 * "return a basic land you control to its owner's hand"). Requires bouncing {@code count}
 * permanents matching {@code filter} that the caster controls.
 */
public record ReturnPermanentsCost(int count, PermanentPredicate filter) implements CastingCost {
}
