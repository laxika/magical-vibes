package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.XValue;

/**
 * A resolution-time optional generic pay-X effect that draws X cards. The default constructor is
 * the life-gain-capped form used by Well of Lost Dreams; {@link #uncapped()} is used when the
 * oracle text has no event-based upper bound.
 */
public record PayXManaDrawXCardsEffect(boolean capAtEventValue) implements CardDrawingEffect {

    public PayXManaDrawXCardsEffect() {
        this(true);
    }

    public static PayXManaDrawXCardsEffect uncapped() {
        return new PayXManaDrawXCardsEffect(false);
    }

    @Override
    public DynamicAmount drawnCardAmount() {
        return new XValue();
    }
}
