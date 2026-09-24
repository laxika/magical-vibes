package com.github.laxika.magicalvibes.model.effect;

/** Creates a token copy of the permanent that caused the current enter-the-battlefield trigger. */
public record CreateTokenCopyOfEnteringPermanentEffect() implements CardEffect {

    @Override
    public boolean usesEnteringPermanentReference() {
        return true;
    }
}
