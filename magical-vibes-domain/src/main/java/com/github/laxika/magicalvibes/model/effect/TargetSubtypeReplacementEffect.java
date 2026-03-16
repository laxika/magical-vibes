package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Wrapper that picks between a base effect and an upgraded effect based on whether
 * the target permanent has a specific subtype. At resolution time, if the target has
 * {@code subtype}, resolves {@code upgradedEffect}; otherwise resolves {@code baseEffect}.
 * Targeting delegates to both inner effects so target selection works for either path.
 */
public record TargetSubtypeReplacementEffect(
        CardSubtype subtype,
        CardEffect baseEffect,
        CardEffect upgradedEffect
) implements ReplacementConditionalEffect {

    @Override
    public String conditionName() {
        return "target is " + subtype.name().toLowerCase();
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
