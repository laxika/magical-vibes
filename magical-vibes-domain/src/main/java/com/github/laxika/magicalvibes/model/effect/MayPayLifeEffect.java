package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Resolution-time optional life payment that resolves {@code wrapped} when accepted and payable,
 * or {@code elseEffect} when the payment is declined or cannot be made.
 */
public record MayPayLifeEffect(int lifeCost, CardEffect wrapped, String prompt,
                               MayPayPayer payer, CardEffect elseEffect)
        implements CardEffect, DyingCreatureCardAwareEffect {

    public MayPayLifeEffect(int lifeCost, CardEffect wrapped, String prompt) {
        this(lifeCost, wrapped, prompt, MayPayPayer.CONTROLLER, null);
    }

    public MayPayLifeEffect(int lifeCost, CardEffect wrapped, String prompt, MayPayPayer payer) {
        this(lifeCost, wrapped, prompt, payer, null);
    }

    @Override
    public TargetSpec targetSpec() {
        if (wrapped != null) {
            TargetSpec wrappedSpec = wrapped.targetSpec();
            if (wrappedSpec != TargetSpec.NONE) {
                return wrappedSpec;
            }
        }
        return elseEffect == null ? TargetSpec.NONE : elseEffect.targetSpec();
    }

    @Override
    public CardEffect boundToDyingCard(UUID dyingCardId) {
        CardEffect boundWrapped = wrapped instanceof DyingCreatureCardAwareEffect aware
                ? aware.boundToDyingCard(dyingCardId) : wrapped;
        CardEffect boundElse = elseEffect instanceof DyingCreatureCardAwareEffect aware
                ? aware.boundToDyingCard(dyingCardId) : elseEffect;
        return new MayPayLifeEffect(lifeCost, boundWrapped, prompt, payer, boundElse);
    }
}
