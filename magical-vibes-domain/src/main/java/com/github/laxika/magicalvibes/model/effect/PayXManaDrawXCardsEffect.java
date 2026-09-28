package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.XValue;

/**
 * A resolution-time optional pay-X effect that draws X cards. The default constructor is the
 * life-gain-capped generic-cost form used by Well of Lost Dreams; {@link #uncapped()} is used when
 * the oracle text has no event-based upper bound. The string constructor supports costs with
 * colored mana in addition to X.
 */
public record PayXManaDrawXCardsEffect(DynamicAmount maximumX, boolean capAtEventValue, String manaCost)
        implements CardDrawingEffect {

    public PayXManaDrawXCardsEffect() {
        this(null, true, "{X}");
    }

    public PayXManaDrawXCardsEffect(DynamicAmount maximumX) {
        this(maximumX, false, "{X}");
    }

    public PayXManaDrawXCardsEffect(String manaCost) {
        this(null, false, manaCost);
    }

    public static PayXManaDrawXCardsEffect uncapped() {
        return new PayXManaDrawXCardsEffect(null, false, "{X}");
    }

    @Override
    public DynamicAmount drawnCardAmount() {
        return new XValue();
    }
}
