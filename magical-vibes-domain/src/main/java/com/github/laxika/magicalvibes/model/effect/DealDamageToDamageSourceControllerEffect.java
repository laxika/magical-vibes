package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.UUID;

/** Deals damage to the controller of the source that caused the triggering ability. */
public record DealDamageToDamageSourceControllerEffect(int amount,
                                                       UUID damageSourcePermanentId,
                                                       UUID damageSourceControllerId)
        implements DamageDealingEffect {

    public DealDamageToDamageSourceControllerEffect(int amount) {
        this(amount, null, null);
    }

    @Override
    public DynamicAmount damageAmount() {
        return new Fixed(amount);
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
