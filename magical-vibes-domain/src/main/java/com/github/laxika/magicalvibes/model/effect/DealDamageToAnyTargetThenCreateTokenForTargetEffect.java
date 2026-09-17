package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Deals damage to any target, then creates the supplied token under that target's control or,
 * when the target is a permanent, under that permanent's controller's control.
 */
public record DealDamageToAnyTargetThenCreateTokenForTargetEffect(
        DynamicAmount damage,
        CreateTokenEffect tokenEffect) implements DamageDealingEffect, TokenCreatingEffect {

    public DealDamageToAnyTargetThenCreateTokenForTargetEffect(int damage, CreateTokenEffect tokenEffect) {
        this(new Fixed(damage), tokenEffect);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.anyTarget());
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

    @Override
    public DynamicAmount tokenAmount() {
        return tokenEffect.amount();
    }

    @Override
    public CardType tokenType() {
        return tokenEffect.primaryType();
    }

    @Override
    public int tokenPower() {
        return tokenEffect.tokenPower();
    }

    @Override
    public int tokenToughness() {
        return tokenEffect.tokenToughness();
    }
}
