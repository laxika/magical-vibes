package com.github.laxika.magicalvibes.model.effect;

/**
 * Prompts for a creature type, then creates a token copy of each matching creature controlled by
 * the effect controller. The matching creatures are snapshotted before any tokens are created.
 */
public record CreateTokenCopyOfEachCreatureOfChosenTypeEffect() implements CardEffect {
}
