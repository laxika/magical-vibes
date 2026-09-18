package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Deals damage to the controller when the permanent sacrificed for the resolving ability matches a filter. */
public record DealDamageToControllerIfSacrificedCardMatchesEffect(
        CardPredicate filter, DynamicAmount amount) implements DamageDealingEffect {

    public DealDamageToControllerIfSacrificedCardMatchesEffect(CardPredicate filter, int damage) {
        this(filter, new Fixed(damage));
    }

    @Override
    public DynamicAmount damageAmount() {
        return amount;
    }

    @Override
    public boolean canDamageCreatures() {
        return false;
    }

    @Override
    public boolean canDamagePlayers() {
        return true;
    }

    @Override
    public boolean damagesController() {
        return true;
    }
}
