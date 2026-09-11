package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a card that shares no name with any unlocked door of a Room controlled by the
 * evaluating player. Requires game state and the evaluating player's id.
 */
public record CardDoesNotShareNameWithControlledRoomPredicate() implements CardPredicate {
}
