package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement marker that adds one 1/1 white Soldier creature token to each creature-token
 * creation event under the source permanent's controller.
 */
public record AddSoldierTokenToCreatureTokenCreationEffect() implements CardEffect {
}
