package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Exile exactly one permanent matching {@code filter} as a casting cost.
 *
 * @param filter the permanent the caster must exile
 * @param label human-readable quality for prompts and errors
 */
public record ExilePermanentCastingCost(PermanentPredicate filter, String label) implements CastingCost {
}
