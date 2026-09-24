package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement marker that adds one 1/1 green Squirrel creature token for each token in a
 * token-creation event under the controller's control.
 */
public record AddSquirrelTokenToTokenCreationEffect() implements CardEffect {
}
