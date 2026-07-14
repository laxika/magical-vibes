package com.github.laxika.magicalvibes.model.effect;

/**
 * Controller draws {@code drawCount} cards, then chooses {@code putCount} cards from their hand
 * and puts them <em>all</em> on top of, or <em>all</em> on the bottom of, their library (a single
 * top/bottom destination applied to every chosen card, not split). Dream Cache: draw 3, put 2 back.
 */
public record DrawThenPutCardsFromHandOnTopOrBottomOfLibraryEffect(int drawCount, int putCount)
        implements CardEffect {
}
