package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.Condition;

import java.util.Objects;

/**
 * Discards the controller's entire hand, draws that many cards, then deals that many damage to
 * each opponent when {@link #condition()} is met.
 */
public record DiscardOwnHandThenDrawThatManyAndDealDamageEffect(Condition condition)
        implements DamageDealingEffect, CardDrawingEffect {

    public DiscardOwnHandThenDrawThatManyAndDealDamageEffect {
        Objects.requireNonNull(condition, "condition");
    }

    @Override
    public DynamicAmount damageAmount() {
        return new EventValue();
    }

    @Override
    public DynamicAmount drawnCardAmount() {
        return new EventValue();
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
