package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.TargetPower;

/** Deals damage equal to the enchanted creature's power to the source Aura's controller. */
public record EnchantedCreatureDealsPowerDamageToControllerEffect() implements DamageDealingEffect {

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
