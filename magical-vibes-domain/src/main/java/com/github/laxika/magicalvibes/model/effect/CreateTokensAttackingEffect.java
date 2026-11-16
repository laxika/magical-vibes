package com.github.laxika.magicalvibes.model.effect;

/**
 * Creates tokens attacking after their attack targets are chosen, using the token blueprint for their tapped state.
 *
 * @param amount                          how many, unless {@code thatMany}
 * @param thatMany                        whether it is as many as the trigger event's count, the stack entry's event
 *                                        value ("create that many tokens", Basri Ket)
 */
public record CreateTokensAttackingEffect(int amount, CreateTokenEffect tokenEffect, boolean sacrificeAtEndStep,
                                          boolean useTriggeringPermanentController, boolean thatMany)
        implements CardEffect {

    public CreateTokensAttackingEffect(int amount, CreateTokenEffect tokenEffect, boolean sacrificeAtEndStep,
                                       boolean useTriggeringPermanentController) {
        this(amount, tokenEffect, sacrificeAtEndStep, useTriggeringPermanentController, false);
    }

    /** As many of {@code tokenEffect}'s tokens as the trigger event's count says. */
    public static CreateTokensAttackingEffect thatMany(CreateTokenEffect tokenEffect) {
        return new CreateTokensAttackingEffect(0, tokenEffect, false, false, true);
    }

    public CreateTokensAttackingEffect(int amount, CreateTokenEffect tokenEffect) {
        this(amount, tokenEffect, false, false);
    }

    public CreateTokensAttackingEffect(int amount, CreateTokenEffect tokenEffect, boolean sacrificeAtEndStep) {
        this(amount, tokenEffect, sacrificeAtEndStep, false);
    }
}
