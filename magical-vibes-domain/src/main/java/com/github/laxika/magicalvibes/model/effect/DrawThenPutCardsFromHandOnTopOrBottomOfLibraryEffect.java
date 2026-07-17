package com.github.laxika.magicalvibes.model.effect;

/**
 * Controller draws {@code drawCount} cards, then chooses {@code putCount} cards from their hand
 * and puts them back on their library. When {@code topOnly} is {@code false}, all chosen cards go
 * <em>all</em> on top of, or <em>all</em> on the bottom of, the library (a single top/bottom
 * destination applied to every chosen card, not split) — Dream Cache: draw 3, put 2 back. When
 * {@code topOnly} is {@code true}, the chosen cards always go on top (in the chosen order, no
 * top/bottom prompt) — Brainstorm: draw 3, put 2 on top in any order.
 */
public record DrawThenPutCardsFromHandOnTopOrBottomOfLibraryEffect(int drawCount, int putCount, boolean topOnly)
        implements CardEffect {

    public DrawThenPutCardsFromHandOnTopOrBottomOfLibraryEffect(int drawCount, int putCount) {
        this(drawCount, putCount, false);
    }
}
