package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Wrapper that picks between a base effect and an upgraded effect based on whether
 * the target permanent matches a predicate at resolution time.
 */
public record TargetPermanentReplacementEffect(
        PermanentPredicate filter,
        CardEffect baseEffect,
        CardEffect upgradedEffect
) implements ReplacementConditionalEffect {

    @Override
    public String conditionName() {
        return "target matches " + filter;
    }

    @Override
    public boolean canTargetPlayer() {
        return baseEffect.canTargetPlayer() || upgradedEffect.canTargetPlayer();
    }

    @Override
    public boolean canTargetPermanent() {
        return baseEffect.canTargetPermanent() || upgradedEffect.canTargetPermanent();
    }

    @Override
    public boolean canTargetSpell() {
        return baseEffect.canTargetSpell() || upgradedEffect.canTargetSpell();
    }

    @Override
    public boolean canTargetGraveyard() {
        return baseEffect.canTargetGraveyard() || upgradedEffect.canTargetGraveyard();
    }
}

