package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Replacement wrapper: picks between a base effect and an upgraded effect based on
 * whether the controller controls a permanent with the specified subtype.
 * <p>
 * Used for planeswalker-signature spells like "If you control a Huatli planeswalker,
 * that creature gets +4/+0 until end of turn instead."
 */
public record ControlsSubtypeReplacementEffect(CardSubtype subtype, CardEffect baseEffect,
                                                CardEffect upgradedEffect) implements ReplacementConditionalEffect {

    @Override
    public String conditionName() {
        return "controls a " + subtype.getDisplayName();
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
