package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "Sacrifice {@code count} [matching permanents]. If you do, {@code thenEffect}."
 *
 * <p>The sacrifice is all-or-nothing. When more than {@code count} matching permanents are
 * available, the controller chooses exactly {@code count}; the follow-up is resolved only after
 * all selected permanents have been sacrificed.
 */
public record SacrificePermanentsThenEffect(
        int count,
        PermanentPredicate filter,
        CardEffect thenEffect,
        String permanentDescription
) implements CardEffect {

    public SacrificePermanentsThenEffect {
        if (count <= 0) {
            throw new IllegalArgumentException("SacrificePermanentsThenEffect count must be positive");
        }
        if (filter == null || thenEffect == null || permanentDescription == null) {
            throw new IllegalArgumentException("SacrificePermanentsThenEffect requires all fields");
        }
    }
}
