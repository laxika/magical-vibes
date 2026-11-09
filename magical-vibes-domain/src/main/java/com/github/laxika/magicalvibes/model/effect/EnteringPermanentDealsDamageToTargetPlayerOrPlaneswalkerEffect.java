package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;

/**
 * "It deals [amount] damage to target player or planeswalker", where "it" is the permanent the enter event was about
 * (Aether Charge's Beast; often inside a {@link MayEffect}): the stack entry's triggering permanent deals the damage, as
 * it is now or as it last existed on the battlefield, else (a creature's own "when this enters") the ability's source.
 */
public record EnteringPermanentDealsDamageToTargetPlayerOrPlaneswalkerEffect(
        DynamicAmount amount,
        PlayerRelation playerRelation
) implements DamageDealingEffect {

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
