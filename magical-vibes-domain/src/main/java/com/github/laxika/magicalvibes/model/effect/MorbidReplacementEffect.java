package com.github.laxika.magicalvibes.model.effect;

/**
 * Wrapper that picks between a base effect and an upgraded effect based on morbid.
 * At resolution time, if a creature died this turn, resolves {@code morbidEffect};
 * otherwise resolves {@code baseEffect}.
 * Targeting delegates to both inner effects so target selection works for either path.
 */
public record MorbidReplacementEffect(CardEffect baseEffect, CardEffect morbidEffect) implements ReplacementConditionalEffect {

    @Override
    public CardEffect upgradedEffect() {
        return morbidEffect;
    }

    @Override
    public String conditionName() {
        return "morbid";
    }

    @Override
    public boolean canTargetPlayer() {
        return baseEffect.canTargetPlayer() || morbidEffect.canTargetPlayer();
    }

    @Override
    public boolean canTargetPermanent() {
        return baseEffect.canTargetPermanent() || morbidEffect.canTargetPermanent();
    }

    @Override
    public boolean canTargetSpell() {
        return baseEffect.canTargetSpell() || morbidEffect.canTargetSpell();
    }

    @Override
    public boolean canTargetGraveyard() {
        return baseEffect.canTargetGraveyard() || morbidEffect.canTargetGraveyard();
    }
}
