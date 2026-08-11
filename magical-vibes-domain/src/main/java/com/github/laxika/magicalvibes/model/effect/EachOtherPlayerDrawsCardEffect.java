package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Each player other than the stack entry's controller draws {@code amount} cards.
 */
public record EachOtherPlayerDrawsCardEffect(DynamicAmount amount) implements CardDrawingEffect {

    public EachOtherPlayerDrawsCardEffect(int amount) {
        this(new Fixed(amount));
    }

    @Override
    public DynamicAmount drawnCardAmount() {
        return amount;
    }
}
