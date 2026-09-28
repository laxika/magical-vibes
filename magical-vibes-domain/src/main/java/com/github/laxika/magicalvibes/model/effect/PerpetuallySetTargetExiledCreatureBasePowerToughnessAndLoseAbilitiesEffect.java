package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

/** Perpetually makes a target creature card exiled with this source a vanilla 1/1. */
public record PerpetuallySetTargetExiledCreatureBasePowerToughnessAndLoseAbilitiesEffect()
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(
                TargetPredicates.exiledCards(new CardTypePredicate(CardType.CREATURE)));
    }
}
