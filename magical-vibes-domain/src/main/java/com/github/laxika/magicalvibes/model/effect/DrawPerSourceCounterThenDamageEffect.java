package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Opponent draw-step trigger (Malignant Growth): the draw-step player (the stack entry's target)
 * draws an additional card for each {@code counterType} counter on the source permanent, then the
 * source deals that much damage to that player. Both halves read the same counter count, so they
 * are one effect rather than a draw effect plus a damage effect.
 *
 * @param counterType the counter type on the source that sets the number of extra draws
 */
public record DrawPerSourceCounterThenDamageEffect(CounterType counterType)
        implements DamageDealingEffect, OpponentDrawStepOnlyEffect {

    @Override
    public DynamicAmount damageAmount() {
        return new CountersOnSource(counterType);
    }

    @Override
    public boolean canDamageCreatures() {
        return false;
    }

    @Override
    public boolean canDamagePlayers() {
        return true;
    }
}
