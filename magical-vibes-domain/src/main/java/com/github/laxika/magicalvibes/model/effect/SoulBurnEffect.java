package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.XValue;

/**
 * Soul Burn: deals X damage to any target, then the controller gains life equal to the damage
 * dealt, capped by (a) black mana spent on X, and (b) the target's life total / loyalty /
 * toughness before the damage was dealt.
 *
 * <p>Implements both {@link DamageDealingEffect} and {@link LifeGainEffect} so the AI sees both
 * facts; {@link #lifeGainAmount()} reports X (upper bound before caps).
 */
public record SoulBurnEffect() implements DamageDealingEffect, LifeGainEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.ANY_TARGET);
    }

    @Override
    public DynamicAmount damageAmount() {
        return new XValue();
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
    public DynamicAmount lifeGainAmount() {
        return new XValue();
    }
}
