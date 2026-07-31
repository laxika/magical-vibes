package com.github.laxika.magicalvibes.model.effect;

/**
 * "Shuffle {@code count} cards from your hand into your library. If you do, [thenEffect]."
 *
 * <p>At resolution the controller chooses {@code count} cards from their hand (mandatory — a short
 * answer is filled from the remaining hand cards); they are shuffled into their library and
 * {@code thenEffect} is pushed onto the stack as a reflexive triggered ability. With an empty hand
 * nothing is shuffled, so {@code thenEffect} never happens. Lat-Nam's Legacy.
 *
 * @param count      number of hand cards to shuffle in (capped at hand size)
 * @param thenEffect effect to execute after a successful shuffle
 */
public record ShuffleCardsFromHandIntoLibraryThenEffect(
        int count,
        CardEffect thenEffect
) implements CardEffect {

    public ShuffleCardsFromHandIntoLibraryThenEffect(CardEffect thenEffect) {
        this(1, thenEffect);
    }
}
