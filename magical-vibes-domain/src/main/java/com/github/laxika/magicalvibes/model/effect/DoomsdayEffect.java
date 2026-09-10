package com.github.laxika.magicalvibes.model.effect;

/**
 * Doomsday: "Search your library and graveyard for five cards and exile the rest. Put the
 * chosen cards on top of your library in any order. You lose half your life, rounded up."
 *
 * <p>The handler holds the controller's library and graveyard as a combined pool, then begins a
 * {@code DoomsdayChoice} for five cards, or all cards if fewer are available. The unchosen cards
 * are exiled. A subsequent life-loss effect resumes after the chosen cards have been ordered.
 */
public record DoomsdayEffect() implements CardEffect {
}
