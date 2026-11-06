package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches permanents controlled by the player the triggered ability's event was about
 * ({@link FilterContext#triggeringPlayerId()}, the stack entry's triggering player): "target creature that player
 * controls" of a "deals damage to a player" trigger, where that player is the player dealt damage. The target is
 * chosen with the event's player as the ability goes on the stack (CR 603.3d) and checked against the entry's
 * triggering player as it resolves. Matches nothing without one.
 */
public record PermanentControlledByTriggeringPlayerPredicate() implements PermanentPredicate {
}
