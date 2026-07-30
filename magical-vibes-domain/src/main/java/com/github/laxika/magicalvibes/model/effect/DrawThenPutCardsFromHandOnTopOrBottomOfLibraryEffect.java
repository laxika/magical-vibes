package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.HandToLibraryPlacement;

/**
 * Controller draws {@code drawCount} cards, then chooses {@code putCount} cards from their hand
 * and puts them back on their library at {@code placement}. With
 * {@link HandToLibraryPlacement#PLAYER_CHOICE} all chosen cards go <em>all</em> on top of, or
 * <em>all</em> on the bottom of, the library (a single top/bottom destination applied to every
 * chosen card, not split) — Dream Cache: draw 3, put 2 back. {@code TOP} always places them on top
 * in the chosen order with no destination prompt (Brainstorm: draw 3, put 2 on top), and
 * {@code BOTTOM} always places them on the bottom (Amass the Components: draw 3, put 1 on the
 * bottom).
 */
public record DrawThenPutCardsFromHandOnTopOrBottomOfLibraryEffect(
        int drawCount, int putCount, HandToLibraryPlacement placement) implements CardEffect {

    public DrawThenPutCardsFromHandOnTopOrBottomOfLibraryEffect(int drawCount, int putCount) {
        this(drawCount, putCount, HandToLibraryPlacement.PLAYER_CHOICE);
    }
}
