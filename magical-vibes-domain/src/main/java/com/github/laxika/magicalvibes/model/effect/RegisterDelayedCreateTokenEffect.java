package com.github.laxika.magicalvibes.model.effect;

/**
 * When resolved, registers a delayed trigger that fires at the beginning of the next end step,
 * creating the token(s) described by {@code tokenEffect} under the resolving controller's control.
 * Used for "create a ... token at the beginning of the next end step" (e.g. Rukh Egg).
 *
 * @param tokenEffect the token-creation effect to resolve at the next end step
 */
public record RegisterDelayedCreateTokenEffect(CreateTokenEffect tokenEffect) implements CardEffect {
}
