package com.github.laxika.magicalvibes.model.effect;

/** Creates a token copy of the permanent that caused the current enter-the-battlefield trigger. */
public record CreateTokenCopyOfEnteringPermanentEffect(
        boolean grantHaste,
        boolean exileAtEndStep
) implements CardEffect {

    public CreateTokenCopyOfEnteringPermanentEffect() {
        this(false, false);
    }

    @Override
    public boolean usesEnteringPermanentReference() {
        return true;
    }
}
