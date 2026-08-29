package com.github.laxika.magicalvibes.model.effect;

/** Registers a delayed trigger that creates tokens for nontoken creatures attacking this turn. */
public record RegisterDelayedNontokenAttackTokenEffect(CreateTokenEffect tokenEffect) implements CardEffect {
}
