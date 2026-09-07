package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** Deals damage to a creature or planeswalker, then sacrifices a land if a nonred permanent was dealt damage. */
public record DealDamageToTargetCreatureOrPlaneswalkerThenSacrificeLandIfNonredEffect(
        DynamicAmount damage) implements DamageDealingEffect {

    public DealDamageToTargetCreatureOrPlaneswalkerThenSacrificeLandIfNonredEffect(int damage) {
        this(new Fixed(damage));
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creatureOrPlaneswalker());
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
