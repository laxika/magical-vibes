package com.github.laxika.magicalvibes.model.effect;

/**
 * Exchanges the controller's hand and library without drawing cards.
 *
 * <p>The old library moves directly to the hand, and the old hand moves directly to the library.
 * A separate {@link ShuffleLibraryEffect} can then randomize the new library.</p>
 */
public record ExchangeHandAndLibraryEffect() implements CardEffect {
}
