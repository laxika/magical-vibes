package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "Choose target permanent that player controls. The player sacrifices that permanent." where
 * "that player" is the damaged player. Mandatory combat damage trigger (sacrifice variant of
 * {@link DestroyPermanentDamagedPlayerControlsEffect}). Only fires when the source dealt at least
 * {@code minimumDamage} damage to the player and that player controls a matching permanent.
 * The source's controller chooses the permanent; the damaged player sacrifices it (ignores
 * indestructible/regeneration).
 * Context: StackEntry.targetId = damaged player ID, StackEntry.sourcePermanentId = source creature ID.
 *
 * @param predicate      filter restricting valid choices (e.g. {@code PermanentIsCreaturePredicate})
 * @param minimumDamage  the trigger only fires if this much damage or more was dealt
 */
public record SacrificePermanentDamagedPlayerControlsEffect(PermanentPredicate predicate, int minimumDamage)
        implements CardEffect {
}
