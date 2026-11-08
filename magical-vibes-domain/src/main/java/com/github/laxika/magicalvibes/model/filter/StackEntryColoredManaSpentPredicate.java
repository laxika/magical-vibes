package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a spell that had colored mana spent to cast it (the colors recorded as it was cast; a copy that wasn't cast,
 * or a spell cast without spending mana, has none). {@code new StackEntryNotPredicate(this)} is "if no colored mana was
 * spent to cast it" (Void Mirror), a cast trigger's stack-entry filter: what was spent can't change once the spell is
 * cast, so judging it as the ability triggers is judging it as it resolves too. Evaluated by
 * {@code TargetLegalityService}, which reads the mana spent from the game.
 */
public record StackEntryColoredManaSpentPredicate() implements StackEntryPredicate {
}
