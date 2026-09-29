package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Each player reveals the top card of their library. For each nonland card revealed this way,
 * the controller creates one copy of {@code token}, then each player draws a card.
 */
public record ParleyCreateTokensEffect(CreateTokenEffect token) implements CardDrawingEffect {

    @Override
    public DynamicAmount drawnCardAmount() {
        return new Fixed(1);
    }
}
