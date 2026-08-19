package com.github.laxika.magicalvibes.model.effect;

/**
 * Each opponent discards one card; for each opponent unable to discard, the controller gains
 * {@code lifeGain} life.
 */
public record EachOpponentDiscardsOrControllerGainsLifeEffect(int lifeGain) implements CardEffect {
}
