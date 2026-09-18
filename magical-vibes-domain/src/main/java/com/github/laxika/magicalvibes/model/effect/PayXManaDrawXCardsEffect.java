package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.XValue;

/**
 * "You may pay {X}, where X is less than or equal to the amount of life you gained. If you do,
 * draw X cards." The life-gain amount is snapshotted on the triggered stack entry, while X is
 * chosen and paid during resolution.
 *
 * @param maximumX optional resolution-time cap for cards whose life-gain amount is not a trigger
 *                event value; {@code null} uses the stack entry's snapshotted event value
 */
public record PayXManaDrawXCardsEffect(DynamicAmount maximumX) implements CardDrawingEffect {

    public PayXManaDrawXCardsEffect() {
        this(null);
    }

    @Override
    public DynamicAmount drawnCardAmount() {
        return new XValue();
    }
}
