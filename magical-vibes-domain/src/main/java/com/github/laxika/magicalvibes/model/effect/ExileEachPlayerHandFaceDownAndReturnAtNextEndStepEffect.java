package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles each player's hand face down and schedules the cards to return to their owners' hands at
 * the beginning of the next end step, after those players discard their current hands.
 */
public record ExileEachPlayerHandFaceDownAndReturnAtNextEndStepEffect() implements CardEffect {
}
