package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect on how its controller draws: "you draw cards from the bottom of your library instead of the
 * top" ({@link From#BOTTOM}: Lantern of Undersight, River Song) or "you may draw cards from anywhere in your library"
 * ({@link From#ANY_POSITION}: Heart of a Duelist, a position chosen for each draw without looking). Each draw is
 * replaced on its own (CR 121.2). Read by {@code DrawService} through the active static effects of the drawing
 * player's permanents, so a face-down permanent or one that lost its abilities has none.
 *
 * @param from where in the library each card is drawn from
 */
public record DrawFromBottomOfLibraryEffect(From from) implements CardEffect {

    /** Where the replaced draw takes the card from. */
    public enum From {
        /** The bottom card of the library. */
        BOTTOM,
        /** A position the drawing player chooses for each draw, the top or bottom included. */
        ANY_POSITION
    }

    public DrawFromBottomOfLibraryEffect {
        if (from == null) {
            from = From.BOTTOM;
        }
    }

    /** "You draw cards from the bottom of your library." */
    public DrawFromBottomOfLibraryEffect() {
        this(From.BOTTOM);
    }

    /** "You may draw cards from anywhere in your library." */
    public static DrawFromBottomOfLibraryEffect fromAnywhere() {
        return new DrawFromBottomOfLibraryEffect(From.ANY_POSITION);
    }
}
