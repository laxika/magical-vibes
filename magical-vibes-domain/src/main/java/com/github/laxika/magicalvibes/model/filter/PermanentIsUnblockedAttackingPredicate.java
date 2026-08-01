package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches an unblocked attacking creature: the current step is at/after declare blockers, the
 * creature is attacking, and no creature is blocking it (and it has not been made blocked by an
 * effect such as Dazzling Beauty). The complement of {@link PermanentIsBlockedPredicate} among
 * attackers. "Unblocked" does not apply before blockers are declared.
 */
public record PermanentIsUnblockedAttackingPredicate() implements PermanentPredicate {
}
