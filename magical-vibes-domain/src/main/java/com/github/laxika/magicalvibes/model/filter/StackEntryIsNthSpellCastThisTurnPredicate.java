package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches the spell that is the {@code spellNumber}-th spell cast this turn by any player
 * ("counter target spell that's the second spell cast this turn" — Second Guess). Evaluated against
 * the global cast order recorded in {@code GameData.getSpellCastOrdinalThisTurn}, so copies put onto
 * the stack without being cast never match.
 *
 * @param spellNumber the 1-based position in this turn's cast order (2 for "the second spell")
 */
public record StackEntryIsNthSpellCastThisTurnPredicate(int spellNumber) implements StackEntryPredicate {
}
