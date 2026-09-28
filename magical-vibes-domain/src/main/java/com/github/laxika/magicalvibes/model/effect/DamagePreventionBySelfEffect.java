package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Capability for static effects that prevent damage dealt by their source to matching creatures. */
public interface DamagePreventionBySelfEffect extends CardEffect {

    /** Returns the additional filter for creatures receiving the prevented damage. */
    PermanentPredicate targetFilter();

    /** Whether this prevention applies only to combat damage. */
    default boolean combatOnly() {
        return false;
    }

    /** Whether preventing combat damage also shuffles the recipient into its owner's library. */
    default boolean shuffleTargetIntoOwnersLibrary() {
        return false;
    }
}
