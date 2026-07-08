package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a permanent if it is a creature with the greatest mana value among all creatures
 * on the battlefield (across every player's battlefield). Multiple creatures can match if
 * tied for greatest mana value. Requires game data to evaluate.
 */
public record PermanentHasGreatestManaValueAmongAllCreaturesPredicate() implements PermanentPredicate {
}
