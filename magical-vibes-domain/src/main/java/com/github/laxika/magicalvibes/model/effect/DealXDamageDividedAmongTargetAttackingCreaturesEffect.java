package com.github.laxika.magicalvibes.model.effect;

public record DealXDamageDividedAmongTargetAttackingCreaturesEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
