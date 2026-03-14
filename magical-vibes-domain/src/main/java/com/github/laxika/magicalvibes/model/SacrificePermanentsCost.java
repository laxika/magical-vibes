package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * A sacrifice component of a casting cost (e.g. sacrifice 3 black creatures for Demon of Death's Gate).
 */
public record SacrificePermanentsCost(int count, PermanentPredicate filter) implements CastingCost {
}
