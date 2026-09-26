package com.github.laxika.magicalvibes.model.effect;

/**
 * Creates a tapped and attacking token copy of each creature token controlled by the resolver
 * that entered the battlefield this turn. The created copies are sacrificed at the next end step.
 */
public record CreateTokenCopyOfEachTokenEnteredThisTurnEffect() implements CardEffect {
}
