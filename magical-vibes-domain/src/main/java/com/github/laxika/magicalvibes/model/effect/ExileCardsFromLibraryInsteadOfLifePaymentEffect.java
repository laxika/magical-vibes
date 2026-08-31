package com.github.laxika.magicalvibes.model.effect;

/** Replaces a life payment with exiling the same number of cards from the payer's library. */
public record ExileCardsFromLibraryInsteadOfLifePaymentEffect()
        implements LifePaymentReplacementEffect {
}
