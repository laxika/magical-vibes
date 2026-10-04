package com.github.laxika.magicalvibes.model.effect;

/** Creates tokens attacking after their attack targets are chosen, using the token blueprint for their tapped state. */
public record CreateTokensAttackingEffect(int amount, CreateTokenEffect tokenEffect, boolean sacrificeAtEndStep,
                                          boolean useTriggeringPermanentController)
        implements CardEffect {

    public CreateTokensAttackingEffect(int amount, CreateTokenEffect tokenEffect) {
        this(amount, tokenEffect, false, false);
    }

    public CreateTokensAttackingEffect(int amount, CreateTokenEffect tokenEffect, boolean sacrificeAtEndStep) {
        this(amount, tokenEffect, sacrificeAtEndStep, false);
    }
}
