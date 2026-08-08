package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Deals damage to a single "any target" (creature, planeswalker or player) chosen uniformly at
 * random as the effect resolves. Nothing is targeted, so no target is declared on activation and
 * shroud/protection do not restrict the pool — protection only prevents the damage itself.
 *
 * <p>Used by Goblin Test Pilot ("{T}: This creature deals 2 damage to any target chosen at
 * random.").
 *
 * @param damage the amount of damage to deal, evaluated at resolution
 */
public record DealDamageToRandomAnyTargetEffect(DynamicAmount damage) implements DamageDealingEffect {

    public DealDamageToRandomAnyTargetEffect(int damage) {
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
        return true;
    }
}
