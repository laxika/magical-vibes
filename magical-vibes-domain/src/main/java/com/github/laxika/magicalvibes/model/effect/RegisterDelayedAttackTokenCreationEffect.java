package com.github.laxika.magicalvibes.model.effect;

/** Registers a delayed trigger that creates tokens whenever the controller attacks this turn. */
public record RegisterDelayedAttackTokenCreationEffect(
        int amount,
        CreateTokenEffect tokenEffect,
        boolean sacrificeAtEndStep
) implements CardEffect {
}
