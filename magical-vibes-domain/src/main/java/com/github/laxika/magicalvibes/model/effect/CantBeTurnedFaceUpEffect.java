package com.github.laxika.magicalvibes.model.effect;

/**
 * Static restriction: the face-down permanents {@code subject} names can't be turned face up in any way, the special
 * action of paying a morph cost (CR 702.37e) included: the enchanted creature (Unable to Scream), or the
 * permanents your opponents control during your turn ({@code ConditionalEffect(ControllerTurn, controlledBy(eachOpponent()))},
 * Karlov Watchdog).
 */
public record CantBeTurnedFaceUpEffect(PermanentSubject subject) implements CardEffect {
}
