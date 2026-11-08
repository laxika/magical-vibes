package com.github.laxika.magicalvibes.model.effect;

/**
 * Creates a token copy of the permanent the trigger event was about: the stack entry's triggering
 * permanent (the permanent that entered, the creature that dealt combat damage), as it is on the
 * battlefield as the ability resolves, else its copiable values as it last existed there.
 *
 * @param removeLegendary "except it isn't legendary"
 */
public record CreateTokenCopyOfEnteringPermanentEffect(
        boolean grantHaste,
        boolean exileAtEndStep,
        boolean sacrificeAtEndStep,
        boolean trackWithSource,
        boolean createForEnteringController,
        boolean removeLegendary
) implements CardEffect {

    public CreateTokenCopyOfEnteringPermanentEffect(boolean grantHaste, boolean exileAtEndStep,
                                                    boolean sacrificeAtEndStep, boolean trackWithSource,
                                                    boolean createForEnteringController) {
        this(grantHaste, exileAtEndStep, sacrificeAtEndStep, trackWithSource, createForEnteringController, false);
    }

    public CreateTokenCopyOfEnteringPermanentEffect(boolean grantHaste, boolean exileAtEndStep,
                                                    boolean sacrificeAtEndStep) {
        this(grantHaste, exileAtEndStep, sacrificeAtEndStep, false, false);
    }

    public CreateTokenCopyOfEnteringPermanentEffect() {
        this(false, false, false);
    }

    public CreateTokenCopyOfEnteringPermanentEffect(boolean grantHaste, boolean exileAtEndStep) {
        this(grantHaste, exileAtEndStep, false);
    }

    /** "Create a token that's a copy of it, except it isn't legendary." */
    public static CreateTokenCopyOfEnteringPermanentEffect nonlegendary() {
        return new CreateTokenCopyOfEnteringPermanentEffect(false, false, false, false, false, true);
    }
}
