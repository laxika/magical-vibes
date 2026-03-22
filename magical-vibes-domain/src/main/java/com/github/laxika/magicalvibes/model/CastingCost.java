package com.github.laxika.magicalvibes.model;

/**
 * A single component of a casting cost. Casting options compose multiple costs
 * (e.g. mana + life, or mana + sacrifice).
 */
public sealed interface CastingCost permits ManaCastingCost, LifeCastingCost, SacrificePermanentsCost, TapUntappedPermanentsCost {
}
