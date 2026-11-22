package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a player who controls a permanent matching {@code filter} ("each player who controls a permanent named
 * Bonder's Ornament"; with {@code Subjects.except}, "each opponent who doesn't control a legendary creature or
 * planeswalker").
 */
public record PlayerControlsPermanentPredicate(PermanentPredicate filter) implements PlayerPredicate {
}
