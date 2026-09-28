package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker for the global damage trigger that makes the other player chosen by the source lose
 * the amount of damage dealt to one of its chosen players.
 */
public record OtherChosenPlayerLosesLifeEffect() implements CardEffect {
}
