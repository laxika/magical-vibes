package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Replacement wrapper: picks between a base effect and an upgraded effect based on
 * whether the controller controls a permanent matching the predicate at resolution time.
 */
public record ControlsPermanentReplacementEffect(
        PermanentPredicate filter,
        CardEffect baseEffect,
        CardEffect upgradedEffect
) implements ReplacementConditionalEffect {

    @Override
    public String conditionName() {
        return "controls a matching permanent";
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

    @Override
    public boolean isDamageOrDestruction() {
        return baseEffect.isDamageOrDestruction() || upgradedEffect.isDamageOrDestruction();
    }
}
