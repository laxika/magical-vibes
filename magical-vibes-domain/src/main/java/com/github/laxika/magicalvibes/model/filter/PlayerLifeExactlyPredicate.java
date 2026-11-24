package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a player whose life total is exactly {@code life} ("each player with exactly 13 life", Triskaidekaphobia).
 * Used with {@code Subjects.matching(eachPlayer(), ...)}; the life totals are read as the effect applies.
 */
public record PlayerLifeExactlyPredicate(int life) implements PlayerPredicate {
}
