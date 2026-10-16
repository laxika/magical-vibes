package com.github.laxika.magicalvibes.model.effect;

/**
 * Goads the permanents created earlier during this same resolution for the rest of the game
 * ("the tokens are goaded for the rest of the game"). The goaded status is not an ability of the
 * permanent, so it persists when the permanent loses its abilities.
 */
public record GoadCreatedPermanentsForRestOfGameEffect() implements CardEffect {
}
