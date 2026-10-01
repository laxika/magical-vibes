package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/** Draws cards and resolves the follow-up only when this effect drew no cards. */
public record DrawCardThenIfNoCardsDrawnEffect(DrawCardEffect drawEffect, CardEffect thenEffect,
                                                Integer drawnCountBefore)
        implements CardDrawingEffect {

    public DrawCardThenIfNoCardsDrawnEffect {
        if (drawEffect == null || thenEffect == null) {
            throw new IllegalArgumentException("DrawCardThenIfNoCardsDrawnEffect requires both effects");
        }
        if (drawEffect.targetSpec() != TargetSpec.NONE || thenEffect.targetSpec() != TargetSpec.NONE) {
            throw new IllegalArgumentException(
                    "DrawCardThenIfNoCardsDrawnEffect requires non-targeting effects");
        }
    }

    public DrawCardThenIfNoCardsDrawnEffect(DynamicAmount amount, CardEffect thenEffect) {
        this(new DrawCardEffect(amount), thenEffect, null);
    }

    public DrawCardThenIfNoCardsDrawnEffect(DrawCardEffect drawEffect, CardEffect thenEffect) {
        this(drawEffect, thenEffect, null);
    }

    public DrawCardThenIfNoCardsDrawnEffect continuation(int drawnCount) {
        return new DrawCardThenIfNoCardsDrawnEffect(drawEffect, thenEffect, drawnCount);
    }

    @Override
    public DynamicAmount drawnCardAmount() {
        return drawEffect.drawnCardAmount();
    }
}
