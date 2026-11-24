package com.github.laxika.magicalvibes.model.filter;

/**
 * "Suspended card" (CR 702.62b): a card in exile that has suspend, printed or gained, and a time counter on it
 * ({@code GameData.isSuspended}); needs the game state. A card exiled face down has no suspend, and a subject judges
 * it without its identity, so it never matches. "Each suspended card" (Dust of Moments), "each suspended card you
 * own" (Talon Gates) over {@code Subjects.exiledCards([players,] this)}.
 */
public record CardIsSuspendedPredicate() implements CardPredicate {
}
