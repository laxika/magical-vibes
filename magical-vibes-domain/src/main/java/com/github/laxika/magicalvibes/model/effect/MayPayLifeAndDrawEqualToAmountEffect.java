package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Offers the controller an optional payment of life equal to {@code amount}; if paid, they draw
 * that many cards.
 */
public record MayPayLifeAndDrawEqualToAmountEffect(DynamicAmount amount) implements CardDrawingEffect {

    public MayPayLifeAndDrawEqualToAmountEffect {
        if (amount == null) {
            throw new IllegalArgumentException("MayPayLifeAndDrawEqualToAmountEffect requires an amount");
        }
    }

    @Override
    public DynamicAmount drawnCardAmount() {
        return amount;
    }
}
