package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Wrapper for conditional effects that apply as long as the controller's graveyard
 * contains at least {@code threshold} cards matching the given {@code filter}
 * (e.g. "As long as there are two or more instant and/or sorcery cards in your graveyard").
 * The wrapped effect only applies while the condition is met.
 */
public record ControllerGraveyardCardThresholdConditionalEffect(
        int threshold,
        CardPredicate filter,
        CardEffect wrapped
) implements ConditionalEffect {

    @Override
    public String conditionName() {
        return "graveyard card threshold (" + threshold + "+)";
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than " + threshold + " matching cards in graveyard";
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
