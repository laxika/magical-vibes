package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Returns all permanents matching the given predicate that the targeted player controls to their
 * owners' hands. The target player is stored in {@code targetId} on the stack entry.
 *
 * <p>Example: River's Rebuke uses {@code PermanentNotPredicate(PermanentIsLandPredicate())} to
 * bounce all nonland permanents the target player controls.
 */
public record ReturnPermanentsTargetPlayerControlsToHandEffect(PermanentPredicate predicate) implements CardEffect {
    @Override public boolean canTargetPlayer() { return true; }
}
