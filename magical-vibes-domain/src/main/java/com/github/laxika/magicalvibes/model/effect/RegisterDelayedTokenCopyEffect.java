package com.github.laxika.magicalvibes.model.effect;

/**
 * Registers a delayed trigger that creates a token copy of the sacrificed artifact ("that artifact"): the stack entry's
 * triggering permanent as it last existed on the battlefield (CR 603.10a).
 */
public record RegisterDelayedTokenCopyEffect() implements CardEffect {
}
