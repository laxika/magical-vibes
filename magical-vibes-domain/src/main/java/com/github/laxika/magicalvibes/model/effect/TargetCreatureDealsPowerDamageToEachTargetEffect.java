package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.TargetPower;

/**
 * The creature chosen for one target group deals damage equal to its power to each permanent
 * chosen for another target group. The dealing creature is the damage source for every event.
 */
public record TargetCreatureDealsPowerDamageToEachTargetEffect(
        int sourceTargetGroup,
        int victimTargetGroup
) implements DamageDealingEffect {

    public TargetCreatureDealsPowerDamageToEachTargetEffect() {
        this(0, 1);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creatureOrPlaneswalker());
    }

    @Override
    public DynamicAmount damageAmount() {
        return new TargetPower();
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
