package com.github.laxika.magicalvibes.model.effect;

/**
 * For each creature token on the battlefield, its controller creates a token copy of that token.
 * The matching tokens are snapshotted before any copies are created, so the new copies are not
 * copied by the same resolution.
 */
public record CreateTokenCopyOfEachCreatureTokenOnBattlefieldEffect() implements CardEffect {
}
