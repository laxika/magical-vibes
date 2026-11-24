package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a player with the most life or tied for most life: no other player has more life than they do ("choose a
 * player with the most life or tied for most life", The Black Gate). Used with
 * {@code Subjects.matching(eachPlayer(), ...)}; the life totals are read as the effect applies.
 */
public record PlayerHasMostLifePredicate() implements PlayerPredicate {
}
