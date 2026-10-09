package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** Deals damage to target creature or player. */
public record DealDamageToTargetCreatureOrPlayerEffect(DynamicAmount damage, boolean chooseOnResolution)
        implements DamageDealingEffect {

    public DealDamageToTargetCreatureOrPlayerEffect(DynamicAmount damage) {
        this(damage, false);
    }

    /** Chooses a creature or player during resolution without targeting. */
    public static DealDamageToTargetCreatureOrPlayerEffect chooseDuringResolution(DynamicAmount damage) {
        return new DealDamageToTargetCreatureOrPlayerEffect(damage, true);
    }

    public DealDamageToTargetCreatureOrPlayerEffect(int damage) {
        this(new Fixed(damage));
    }

    @Override
    public TargetSpec targetSpec() {
        return chooseOnResolution ? TargetSpec.NONE : TargetSpec.harmful(TargetPredicates.creatureOrPlayer());
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
