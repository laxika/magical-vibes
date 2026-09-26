package com.github.laxika.magicalvibes.model.effect;

/**
 * One-shot effect: the resolving spell's controller can't gain life for the rest of the game.
 * Sets the controller-specific restriction on {@code GameData}.
 */
public record PlayersCantGainLifeRestOfGameEffect() implements CardEffect {
}
