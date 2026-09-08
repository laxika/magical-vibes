package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Deals damage to one creature controlled by an opponent of the effect controller, chosen
 * uniformly at random as the effect resolves. Nothing is targeted.
 *
 * @param damage the amount of damage to deal, evaluated at resolution
 */
public record DealDamageToRandomOpponentCreatureEffect(DynamicAmount damage)
        implements DamageDealingEffect {

    public DealDamageToRandomOpponentCreatureEffect(int damage) {
        this(new Fixed(damage));
    }

    @Override
    public DynamicAmount damageAmount() {
        return damage;
    }

    @Override
    public boolean canDamageCreatures() {
        return true;
    }

    @Override
    public boolean canDamagePlayers() {
        return false;
    }
}
