package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PlayerRelation;

/** Target player sacrifices a nontoken creature, then a duplicate may be conjured into hand. */
public record TargetPlayerSacrificesNontokenCreatureThenConjuresDuplicateEffect(
        PlayerRelation targetPlayerRelation, int maxManaValue, boolean mayDiscard)
        implements CardEffect {

    public TargetPlayerSacrificesNontokenCreatureThenConjuresDuplicateEffect() {
        this(PlayerRelation.OPPONENT, 2, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }

    @Override
    public PlayerRelation targetPlayerRelation() {
        return targetPlayerRelation;
    }
}
