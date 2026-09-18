package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Registers a delayed trigger that creates tokens whenever the controller attacks this turn. */
public record RegisterDelayedAttackTokenCreationEffect(
        int amount,
        CreateTokenEffect tokenEffect,
        boolean sacrificeAtEndStep,
        PermanentPredicate attackerPredicate
) implements CardEffect {

    public RegisterDelayedAttackTokenCreationEffect(int amount, CreateTokenEffect tokenEffect,
                                                     boolean sacrificeAtEndStep) {
        this(amount, tokenEffect, sacrificeAtEndStep, null);
    }
}
