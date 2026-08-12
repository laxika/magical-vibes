package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.XValue;

/**
 * "You may pay {X}, where X is less than or equal to the amount of life you gained. If you do,
 * draw X cards." The life-gain amount is snapshotted on the triggered stack entry, while X is
 * chosen and paid during resolution.
 */
public record PayXManaDrawXCardsEffect() implements CardDrawingEffect {

    @Override
    public DynamicAmount drawnCardAmount() {
        return new XValue();
    }
}
