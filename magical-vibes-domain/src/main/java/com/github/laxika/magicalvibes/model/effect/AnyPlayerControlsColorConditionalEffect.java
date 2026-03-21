package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

/**
 * Conditional wrapper: "as long as any player controls a [color] permanent".
 * The wrapped effect only applies while at least one permanent of the specified
 * color exists on any player's battlefield.
 */
public record AnyPlayerControlsColorConditionalEffect(CardColor color, CardEffect wrapped) implements ConditionalEffect {

    @Override
    public String conditionName() {
        return "any player controls a " + color.name().toLowerCase() + " permanent";
    }

    @Override
    public String conditionNotMetReason() {
        return "no player controls a " + color.name().toLowerCase() + " permanent";
    }

    @Override
    public boolean canTargetPlayer() {
        return wrapped.canTargetPlayer();
    }

    @Override
    public boolean canTargetPermanent() {
        return wrapped.canTargetPermanent();
    }

    @Override
    public boolean canTargetSpell() {
        return wrapped.canTargetSpell();
    }

    @Override
    public boolean canTargetGraveyard() {
        return wrapped.canTargetGraveyard();
    }
}
