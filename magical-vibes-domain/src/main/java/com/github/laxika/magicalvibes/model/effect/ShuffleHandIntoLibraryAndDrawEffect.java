package com.github.laxika.magicalvibes.model.effect;

/**
 * Shuffles cards from one or all players' hands into their libraries, then draws that many cards.
 *
 * @param eachPlayer whether the effect applies to every player; when false, it applies only to the
 *                  resolving entry's controller
 */
public record ShuffleHandIntoLibraryAndDrawEffect(boolean eachPlayer) implements CardEffect {

    public ShuffleHandIntoLibraryAndDrawEffect() {
        this(true);
    }
}
