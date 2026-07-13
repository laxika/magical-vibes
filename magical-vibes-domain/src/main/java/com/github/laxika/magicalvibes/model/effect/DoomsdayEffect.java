package com.github.laxika.magicalvibes.model.effect;

/**
 * Doomsday: "Search your library and graveyard for five cards and exile the rest. Put the
 * chosen cards on top of your library in any order. You lose half your life, rounded up."
 *
 * <p>The handler applies the half-life loss, holds the controller's library and graveyard out
 * as a combined pool, then begins a {@code DoomsdayChoice} for the (up to) five cards to keep
 * on top; the unchosen cards are exiled.
 */
public record DoomsdayEffect() implements CardEffect {
}
