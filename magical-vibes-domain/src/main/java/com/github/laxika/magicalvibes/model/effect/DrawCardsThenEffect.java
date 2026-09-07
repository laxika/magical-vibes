package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Draws the wrapped number of cards and, if that many cards were drawn, puts the follow-up effect
 * on the stack as a reflexive triggered ability.
 */
public record DrawCardsThenEffect(DrawCardEffect drawEffect, CardEffect thenEffect)
        implements CardDrawingEffect {

    public DrawCardsThenEffect {
        if (drawEffect == null || thenEffect == null) {
            throw new IllegalArgumentException("DrawCardsThenEffect requires both effects");
        }
        if (drawEffect.targetSpec() != TargetSpec.NONE) {
            throw new IllegalArgumentException("DrawCardsThenEffect requires a non-targeting draw effect");
        }
    }

    public DrawCardsThenEffect(DynamicAmount amount, CardEffect thenEffect) {
        this(new DrawCardEffect(amount), thenEffect);
    }

    public DrawCardsThenEffect(int amount, CardEffect thenEffect) {
        this(new DrawCardEffect(amount), thenEffect);
    }

    @Override
    public DynamicAmount drawnCardAmount() {
        return drawEffect.drawnCardAmount();
    }
}
