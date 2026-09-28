package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** Resolves Nihiloor's attack trigger for an attacking creature an opponent owns. */
public record NihiloorAttackEffect() implements LifeGainEffect {

    @Override
    public DynamicAmount lifeGainAmount() {
        return new Fixed(2);
    }
}
