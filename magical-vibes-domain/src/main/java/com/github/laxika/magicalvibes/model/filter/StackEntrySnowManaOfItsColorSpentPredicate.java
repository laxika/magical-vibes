package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a spell that had snow mana of any of its colors spent to cast it ("if {S} of any of that spell's colors was
 * spent to cast it", Boreal Outrider): mana from a snow source whose color is one of the spell's, whichever part of
 * the spell's total cost it paid. It is a cast trigger's stack-entry filter: what was spent can't change once the spell
 * is cast, so judging it as the ability triggers is judging it as it resolves too. Evaluated by
 * {@code TargetLegalityService}, which reads the mana spent from the game.
 */
public record StackEntrySnowManaOfItsColorSpentPredicate() implements StackEntryPredicate {
}
