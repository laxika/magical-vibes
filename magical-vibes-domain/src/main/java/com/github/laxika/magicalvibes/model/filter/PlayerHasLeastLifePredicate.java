package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a player with the lowest life total or tied for it: no other player has less life ("the player with the
 * lowest life total … if two or more players are tied, you choose one of them", Loxodon Peacekeeper). Used with
 * {@code Subjects.matching(eachPlayer(), ...)}; the life totals are read as the effect applies.
 */
public record PlayerHasLeastLifePredicate() implements PlayerPredicate {
}
