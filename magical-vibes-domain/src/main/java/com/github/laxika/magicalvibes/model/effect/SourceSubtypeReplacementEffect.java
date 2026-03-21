package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Wrapper that picks between a base effect and an upgraded effect based on whether
 * the source permanent (the creature activating the ability) has a specific subtype.
 * At resolution time, if the source has {@code subtype}, resolves {@code upgradedEffect};
 * otherwise resolves {@code baseEffect}.
 * Targeting delegates to both inner effects so target selection works for either path.
 */
public record SourceSubtypeReplacementEffect(
        CardSubtype subtype,
        CardEffect baseEffect,
        CardEffect upgradedEffect
) implements ReplacementConditionalEffect {

    @Override
    public String conditionName() {
        return "source is " + subtype.name().toLowerCase();
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
