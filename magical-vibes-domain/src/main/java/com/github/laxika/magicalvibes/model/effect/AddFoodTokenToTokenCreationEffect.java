package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement marker that adds one Food token to each token-creation event under the
 * controller's control.
 */
public record AddFoodTokenToTokenCreationEffect() implements CardEffect {
}
