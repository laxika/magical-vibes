package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "Exile target permanent that player controls" where "that player" is the damaged player.
 * Used inside a MayEffect wrapper for combat damage triggers.
 * Context: StackEntry.targetId = damaged player ID, StackEntry.sourcePermanentId = source creature ID.
 *
 * @param predicate optional filter to restrict valid targets (e.g. black or red permanents)
 */
public record ExilePermanentDamagedPlayerControlsEffect(PermanentPredicate predicate) implements CardEffect {
}
