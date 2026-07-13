package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "Destroy target permanent that player controls" where "that player" is the damaged player.
 * Mandatory combat damage trigger (destroy variant of {@link ExilePermanentDamagedPlayerControlsEffect}).
 * Only fires when the source dealt at least {@code minimumDamage} damage to the player.
 * Context: StackEntry.targetId = damaged player ID, StackEntry.sourcePermanentId = source creature ID.
 *
 * @param predicate      filter restricting valid targets (e.g. {@code PermanentIsLandPredicate} for lands)
 * @param minimumDamage  the trigger only fires if this much damage or more was dealt (Deus of Calamity: 6)
 */
public record DestroyPermanentDamagedPlayerControlsEffect(PermanentPredicate predicate, int minimumDamage)
        implements CardEffect {
}
