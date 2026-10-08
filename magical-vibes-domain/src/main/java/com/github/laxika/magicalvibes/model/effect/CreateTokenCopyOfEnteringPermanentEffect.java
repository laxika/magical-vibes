package com.github.laxika.magicalvibes.model.effect;

/** Creates a token copy of the permanent that caused the current enter-the-battlefield trigger. */
public record CreateTokenCopyOfEnteringPermanentEffect(
        boolean grantHaste,
        boolean exileAtEndStep,
        boolean sacrificeAtEndStep
) implements CardEffect {

    public CreateTokenCopyOfEnteringPermanentEffect() {
        this(false, false, false);
    }

    public CreateTokenCopyOfEnteringPermanentEffect(boolean grantHaste, boolean exileAtEndStep) {
        this(grantHaste, exileAtEndStep, false);
    }

    @Override
    public boolean usesEnteringPermanentReference() {
        return true;
    }
}
