package com.github.laxika.magicalvibes.model.effect;

/**
 * Conditional wrapper for ON_OPPONENT_LAND_ENTERS_BATTLEFIELD triggers: the wrapped effect
 * only fires if the entering permanent's name matches the source permanent's imprinted card name.
 * Used by Invader Parasite.
 */
public record ImprintedCardNameMatchesEnteringPermanentConditionalEffect(
        CardEffect wrapped
) implements ConditionalEffect {

    @Override
    public String conditionName() {
        return "imprinted card name matches";
    }

    @Override
    public String conditionNotMetReason() {
        return "entering permanent name does not match imprinted card";
    }

    @Override
    public boolean canTargetPlayer() {
        return wrapped.canTargetPlayer();
    }
}
