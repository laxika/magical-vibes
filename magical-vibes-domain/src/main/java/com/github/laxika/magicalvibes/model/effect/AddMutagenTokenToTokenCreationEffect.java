package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement marker that adds one Mutagen artifact token to each token-creation event
 * under the source permanent's controller.
 */
public record AddMutagenTokenToTokenCreationEffect() implements CardEffect {
}
