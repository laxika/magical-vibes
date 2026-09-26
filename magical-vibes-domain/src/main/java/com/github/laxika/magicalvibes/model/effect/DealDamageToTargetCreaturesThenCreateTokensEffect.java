package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Deals damage to each chosen target creature, then creates one supplied token for each creature
 * that was actually dealt damage by this effect.
 */
public record DealDamageToTargetCreaturesThenCreateTokensEffect(
        DynamicAmount damage,
        CreateTokenEffect token
) implements CardEffect {

    public DealDamageToTargetCreaturesThenCreateTokensEffect(int damage, CreateTokenEffect token) {
        this(new Fixed(damage), token);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }
}
