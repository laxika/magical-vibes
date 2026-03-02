package com.github.laxika.magicalvibes.model.effect;

/**
 * Wrapper for equipped-conditional effects (static abilities).
 * The wrapped effect only applies as long as this creature has at least one Equipment attached.
 * Analogous to MetalcraftConditionalEffect but with an "is equipped" condition instead of "3+ artifacts".
 */
public record EquippedConditionalEffect(CardEffect wrapped) implements ConditionalEffect {

    @Override
    public String conditionName() {
        return "equipped";
    }

    @Override
    public String conditionNotMetReason() {
        return "not equipped";
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
