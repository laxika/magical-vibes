package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a creature blocking or blocked by the source.
 *
 * @param lastKnownCombatForActivatedAbility if {@code true}, an activated ability whose source has left
 *        the battlefield still sees the combat relationships the source had (Steadfast Armasaur's
 *        ruling: use its last existence on the battlefield); otherwise a source that is gone has no
 *        combat relationships when the activated ability resolves.
 */
public record PermanentInCombatWithSourcePredicate(boolean lastKnownCombatForActivatedAbility)
        implements PermanentPredicate {

    public PermanentInCombatWithSourcePredicate() {
        this(false);
    }
}
