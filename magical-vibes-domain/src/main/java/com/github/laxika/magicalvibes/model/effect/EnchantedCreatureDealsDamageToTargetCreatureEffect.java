package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The creature enchanted by the source Aura deals damage to a targeted creature.
 *
 * @param damage the amount of damage to deal
 * @param targetPredicate an additional restriction on the targeted creature
 */
public record EnchantedCreatureDealsDamageToTargetCreatureEffect(DynamicAmount damage,
                                                                 PermanentPredicate targetPredicate)
        implements DamageDealingEffect {

    public EnchantedCreatureDealsDamageToTargetCreatureEffect(int damage, PermanentPredicate targetPredicate) {
        this(new Fixed(damage), targetPredicate);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature(), targetPredicate);
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
