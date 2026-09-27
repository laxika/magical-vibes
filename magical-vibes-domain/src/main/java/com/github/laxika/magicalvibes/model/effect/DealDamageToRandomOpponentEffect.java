package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;

/** Deals damage to an opponent chosen uniformly at random, with optional targeting. */
public record DealDamageToRandomOpponentEffect(DynamicAmount damage, boolean targeted)
        implements DamageDealingEffect {

    public DealDamageToRandomOpponentEffect(DynamicAmount damage) {
        this(damage, false);
    }

    public DealDamageToRandomOpponentEffect(int damage) {
        this(new Fixed(damage), false);
    }

    public static DealDamageToRandomOpponentEffect targeted(int damage) {
        return new DealDamageToRandomOpponentEffect(new Fixed(damage), true);
    }

    @Override
    public TargetSpec targetSpec() {
        return targeted ? TargetSpec.harmful(TargetPredicates.player()) : TargetSpec.NONE;
    }

    @Override
    public boolean targetChosenAtRandom() {
        return targeted;
    }

    @Override
    public PlayerRelation targetPlayerRelation() {
        return PlayerRelation.OPPONENT;
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
