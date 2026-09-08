package com.github.laxika.magicalvibes.model.effect;

/**
 * Chooses one creature that is blocking or blocked by the source creature and exiles it,
 * tracking the exiled card with the source permanent.
 */
public record ExileOneOfCombatOpponentsAndTrackWithSourceEffect() implements CardEffect {
}
