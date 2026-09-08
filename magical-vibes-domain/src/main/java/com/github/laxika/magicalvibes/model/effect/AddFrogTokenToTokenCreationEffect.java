package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement marker that adds one 1/1 green Frog creature token to each token-creation
 * event under the controller's control.
 */
public record AddFrogTokenToTokenCreationEffect() implements CardEffect {
}
