package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches an unblocked attacking creature: it is attacking and no creature is blocking it (and it
 * has not been made blocked by an effect such as Dazzling Beauty). The complement of
 * {@link PermanentIsBlockedPredicate} among attackers.
 */
public record PermanentIsUnblockedAttackingPredicate() implements PermanentPredicate {
}
