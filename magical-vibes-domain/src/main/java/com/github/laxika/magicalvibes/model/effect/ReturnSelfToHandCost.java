package com.github.laxika.magicalvibes.model.effect;

/**
 * Return this permanent to its owner's hand as an activation cost ("Return this enchantment to its
 * owner's hand: …" — Cycle of Life). Paid by {@code ActivatedAbilityExecutionService} before the
 * ability goes on the stack, so the ability still resolves after the source has left the
 * battlefield.
 */
public record ReturnSelfToHandCost() implements CostEffect {

    @Override
    public boolean consumesSourcePermanent() {
        return true;
    }
}
