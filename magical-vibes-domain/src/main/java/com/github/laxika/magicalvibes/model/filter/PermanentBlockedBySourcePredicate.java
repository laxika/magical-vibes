package com.github.laxika.magicalvibes.model.filter;

/**
 * Creatures the source permanent is blocking — "target creature it's blocking" (Goblin Snowman).
 * Narrower than {@link PermanentInCombatWithSourcePredicate}, which also matches creatures that
 * are blocking the source.
 */
public record PermanentBlockedBySourcePredicate() implements PermanentPredicate {
}
