package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Deals damage to one creature the controller chooses as the effect resolves, without targeting it
 * ("deals 2 damage to another creature you control"). The choice is offered among the creatures
 * matching {@code filter}, judged from the source and its controller; protection and other damage
 * prevention still apply to the damage.
 *
 * @param damage the amount of damage to deal, evaluated at resolution
 * @param filter restriction on the creatures that can be chosen, or {@code null} for any creature
 */
public record DealDamageToChosenCreatureEffect(DynamicAmount damage, PermanentPredicate filter)
        implements DamageDealingEffect {

    public DealDamageToChosenCreatureEffect(int damage, PermanentPredicate filter) {
        this(new Fixed(damage), filter);
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
