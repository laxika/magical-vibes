package com.github.laxika.magicalvibes.model.filter;

/**
 * Creatures blocking the source permanent — "target creature blocking this creature"
 * (Barbed-Back Wurm). Mirror image of {@link PermanentBlockedBySourcePredicate} and narrower
 * than {@link PermanentInCombatWithSourcePredicate}, which matches both directions.
 */
public record PermanentBlockingSourcePredicate() implements PermanentPredicate {
}
