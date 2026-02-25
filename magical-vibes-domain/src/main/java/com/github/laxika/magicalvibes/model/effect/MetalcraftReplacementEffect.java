package com.github.laxika.magicalvibes.model.effect;

/**
 * Wrapper that picks between a base effect and an upgraded effect based on metalcraft.
 * At resolution time, if the controller controls three or more artifacts, resolves
 * {@code metalcraftEffect}; otherwise resolves {@code baseEffect}.
 * Targeting delegates to both inner effects so target selection works for either path.
 */
public record MetalcraftReplacementEffect(CardEffect baseEffect, CardEffect metalcraftEffect) implements CardEffect {

    @Override
    public boolean canTargetPlayer() {
        return baseEffect.canTargetPlayer() || metalcraftEffect.canTargetPlayer();
    }

    @Override
    public boolean canTargetPermanent() {
        return baseEffect.canTargetPermanent() || metalcraftEffect.canTargetPermanent();
    }

    @Override
    public boolean canTargetSpell() {
        return baseEffect.canTargetSpell() || metalcraftEffect.canTargetSpell();
    }

    @Override
    public boolean canTargetGraveyard() {
        return baseEffect.canTargetGraveyard() || metalcraftEffect.canTargetGraveyard();
    }
}
