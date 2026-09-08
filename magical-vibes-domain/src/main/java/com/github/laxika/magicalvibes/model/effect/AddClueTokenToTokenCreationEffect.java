package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect that adds one Clue token to a token-creation event when its source
 * permanent is solved.
 */
public record AddClueTokenToTokenCreationEffect() implements CardEffect {
}
