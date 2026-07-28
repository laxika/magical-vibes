package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a creature that was declared as an attacker during its controller's previous turn. The
 * record is kept per controller-turn (it survives the intervening opponent turns and is shifted at
 * the start of the controller's next turn), unlike
 * {@link PermanentAttackedOrBlockedThisTurnPredicate}, which only covers the current turn.
 * Used by Halls of Mist.
 */
public record PermanentAttackedDuringControllersLastTurnPredicate() implements PermanentPredicate {
}
