package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.TargetPower;

/**
 * The targeted creature deals damage equal to its power to the controller of the resolving
 * ability. The targeted creature is the damage source.
 */
public record TargetCreatureDealsPowerDamageToAbilityControllerEffect() implements DamageDealingEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }

    @Override
    public DynamicAmount damageAmount() {
        return new TargetPower();
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
