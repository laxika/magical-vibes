package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * A player returns a permanent they control matching {@code filter} to its owner's hand, then
 * resolves {@code thenEffect} only after a permanent was actually returned. The permanent is
 * chosen at resolution time, so this effect is non-targeting.
 *
 * @param filter               predicate for permanents the player may return
 * @param thenEffect           effect resolved after the return, or {@code null}
 * @param permanentDescription human-readable description used in the choice prompt
 */
public record ReturnPermanentControlledByPlayerToHandThenEffect(
        PermanentPredicate filter,
        CardEffect thenEffect,
        String permanentDescription
) implements CardEffect {
}
