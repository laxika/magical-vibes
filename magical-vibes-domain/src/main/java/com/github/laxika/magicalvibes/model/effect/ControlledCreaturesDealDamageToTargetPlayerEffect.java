package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Each matching creature controlled by the effect controller deals fixed damage to a target player. */
public record ControlledCreaturesDealDamageToTargetPlayerEffect(
        DynamicAmount damage,
        PermanentPredicate filter
) implements DamageDealingEffect {

    public ControlledCreaturesDealDamageToTargetPlayerEffect(int damage, PermanentPredicate filter) {
        this(new Fixed(damage), filter);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }

    @Override
    public DynamicAmount damageAmount() {
        return damage;
    }

    @Override
    public boolean canDamageCreatures() {
        return false;
    }

    @Override
    public boolean canDamagePlayers() {
        return true;
    }
}
