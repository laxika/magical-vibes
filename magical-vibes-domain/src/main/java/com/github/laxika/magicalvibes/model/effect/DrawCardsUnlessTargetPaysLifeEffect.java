package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PlayerRelation;

/** Draws cards unless the targeted opponent pays a fixed amount of life. */
public record DrawCardsUnlessTargetPaysLifeEffect(int drawCount, int lifeCost)
        implements CardDrawingEffect {

    public DrawCardsUnlessTargetPaysLifeEffect {
        if (drawCount < 0 || lifeCost < 0) {
            throw new IllegalArgumentException("drawCount and lifeCost cannot be negative");
        }
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.players(
                new com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate(PlayerRelation.OPPONENT)));
    }

    @Override
    public com.github.laxika.magicalvibes.model.amount.DynamicAmount drawnCardAmount() {
        return new com.github.laxika.magicalvibes.model.amount.Fixed(drawCount);
    }
}
