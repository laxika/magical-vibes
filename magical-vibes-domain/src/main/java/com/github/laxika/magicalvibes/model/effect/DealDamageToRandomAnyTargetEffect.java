package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Deals damage to "any target chosen at random" (creature, planeswalker or player). The target is
 * chosen uniformly at random among the legal targets as the ability is activated, so players can
 * respond knowing it, and it is rechecked for legality on resolution like any other target.
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
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.anyTarget());
    }

    @Override
    public boolean targetChosenAtRandom() {
        return true;
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
