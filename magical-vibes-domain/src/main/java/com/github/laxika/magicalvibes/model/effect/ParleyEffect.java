package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Each player reveals the top card of their library, then each player draws a card. The default
 * form adds one green mana and gains 1 life for each nonland card; the token form creates the
 * supplied token once per revealed nonland instead.
 *
 * <p>The {@link CardDrawingEffect} capability is intentional: the draw means this ability must
 * resolve through the stack rather than the mana-ability fast path.
 */
public record ParleyEffect(CreateTokenEffect tokenReward) implements CardDrawingEffect, ManaProducingEffect {

    /** Selvala's mana-and-life form. */
    public ParleyEffect() {
        this(null);
    }

    /** Parley form that creates the supplied token for each revealed nonland. */
    public ParleyEffect(CreateTokenEffect tokenReward) {
        this.tokenReward = tokenReward;
    }

    @Override
    public DynamicAmount drawnCardAmount() {
        return new Fixed(1);
    }
}
