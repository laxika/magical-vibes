package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Removes all counters of one type from a referenced permanent, destroys it, and has that
 * permanent deal fixed damage to its controller.
 */
public record RemoveAllCountersThenDestroyReferencedPermanentAndDamageControllerEffect(
        PermanentReference reference, CounterType counterType, int damage)
        implements RemovalEffect, DamageDealingEffect {

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.DESTROY;
    }

    @Override
    public Fixed damageAmount() {
        return new Fixed(damage);
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
