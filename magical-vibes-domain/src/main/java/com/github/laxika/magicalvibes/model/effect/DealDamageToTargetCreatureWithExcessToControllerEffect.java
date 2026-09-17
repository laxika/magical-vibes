package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Deals damage to a target creature, redirecting damage beyond lethal to that creature's
 * controller. Optionally transforms the source permanent when any excess damage is redirected.
 */
public record DealDamageToTargetCreatureWithExcessToControllerEffect(
        DynamicAmount damage,
        boolean transformSourceIfExcess
) implements DamageDealingEffect {

    public DealDamageToTargetCreatureWithExcessToControllerEffect(int damage,
                                                                   boolean transformSourceIfExcess) {
        this(new Fixed(damage), transformSourceIfExcess);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
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
