package com.github.laxika.magicalvibes.model.effect;

/**
 * Wrapper that picks between a base effect and a kicked effect based on whether
 * the spell was kicked. At resolution time, if the spell was kicked, resolves
 * {@code kickedEffect}; otherwise resolves {@code baseEffect}.
 * Targeting delegates to both inner effects so target selection works for either path.
 *
 * <p>Use this for kicker spells where the kicked mode <em>replaces</em> the base mode
 * ("instead" in oracle text), e.g. Fight with Fire.
 */
public record KickerReplacementEffect(CardEffect baseEffect, CardEffect kickedEffect) implements ReplacementConditionalEffect {

    @Override
    public CardEffect upgradedEffect() {
        return kickedEffect;
    }

    @Override
    public String conditionName() {
        return "kicker";
    }

    @Override
    public boolean canTargetPlayer() {
        return baseEffect.canTargetPlayer() || kickedEffect.canTargetPlayer();
    }

    @Override
    public boolean canTargetPermanent() {
        return baseEffect.canTargetPermanent() || kickedEffect.canTargetPermanent();
    }

    @Override
    public boolean canTargetSpell() {
        return baseEffect.canTargetSpell() || kickedEffect.canTargetSpell();
    }

    @Override
    public boolean canTargetGraveyard() {
        return baseEffect.canTargetGraveyard() || kickedEffect.canTargetGraveyard();
    }

    @Override
    public boolean isDamageOrDestruction() {
        return baseEffect.isDamageOrDestruction() || kickedEffect.isDamageOrDestruction();
    }
}
