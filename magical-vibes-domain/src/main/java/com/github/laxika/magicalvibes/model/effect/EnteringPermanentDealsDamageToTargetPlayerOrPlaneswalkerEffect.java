package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;

/**
 * Damage effect for an entering permanent that is made optional by a surrounding {@link MayEffect}.
 * The trigger collector binds the entering permanent as the source before the target is chosen.
 */
public record EnteringPermanentDealsDamageToTargetPlayerOrPlaneswalkerEffect(
        DynamicAmount amount,
        PlayerRelation playerRelation
) implements DamageDealingEffect, TriggeringPermanentSourceEffect {

    public EnteringPermanentDealsDamageToTargetPlayerOrPlaneswalkerEffect(int damage,
                                                                           PlayerRelation playerRelation) {
        this(new Fixed(damage), playerRelation);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.playerOrPlaneswalker());
    }

    @Override
    public PlayerRelation targetPlayerRelation() {
        return playerRelation;
    }

    @Override
    public DynamicAmount damageAmount() {
        return amount;
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
