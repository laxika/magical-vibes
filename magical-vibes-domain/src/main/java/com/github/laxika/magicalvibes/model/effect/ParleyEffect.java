package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Each player reveals the top card of their library. For each nonland card revealed this way,
 * the controller adds one green mana and gains 1 life, then each player draws a card.
 *
 * <p>The {@link CardDrawingEffect} capability is intentional: the draw means this ability must
 * resolve through the stack rather than the mana-ability fast path.
 */
public record ParleyEffect() implements CardDrawingEffect, ManaProducingEffect {

    @Override
    public DynamicAmount drawnCardAmount() {
        return new Fixed(1);
    }
}
