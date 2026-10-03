package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Makes the controller draw and then discard the evaluated number of cards. After the discard
 * finishes, the follow-up effect receives the discarded card ids as its triggering cards.
 */
public record DrawDiscardThenEffect(DynamicAmount amount, CardEffect thenEffect)
        implements CardDrawingEffect {

    public DrawDiscardThenEffect {
        if (amount == null || thenEffect == null) {
            throw new IllegalArgumentException("DrawDiscardThenEffect requires both effects");
        }
    }

    public DrawDiscardThenEffect(int amount, CardEffect thenEffect) {
        this(new Fixed(amount), thenEffect);
    }

    @Override
    public DynamicAmount drawnCardAmount() {
        return amount;
    }
}
