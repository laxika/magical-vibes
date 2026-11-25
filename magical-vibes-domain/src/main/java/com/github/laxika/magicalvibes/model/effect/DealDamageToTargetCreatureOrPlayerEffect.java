package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Deals damage to a creature or player chosen as the effect resolves, without targeting (Comet, Stellar Pup). A
 * targeted "target creature or player" is {@link DealDamageToAnyTargetEffect} under a creature-or-player target
 * filter.
 */
public record DealDamageToTargetCreatureOrPlayerEffect(DynamicAmount damage) implements DamageDealingEffect {

    /** Chooses a creature or player during resolution without targeting. */
    public static DealDamageToTargetCreatureOrPlayerEffect chooseDuringResolution(DynamicAmount damage) {
        return new DealDamageToTargetCreatureOrPlayerEffect(damage);
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
        return true;
    }
}
