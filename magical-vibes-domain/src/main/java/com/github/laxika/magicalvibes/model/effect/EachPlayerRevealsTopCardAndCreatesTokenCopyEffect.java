package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals the top card of each player's library and creates a token copy of each revealed creature
 * using the supplied copy profile. The revealed cards remain on top of their libraries.
 */
public record EachPlayerRevealsTopCardAndCreatesTokenCopyEffect(
        CreateTokenCopyOfTargetPermanentEffect tokenCopyEffect
) implements CardEffect {
}
