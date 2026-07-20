package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a creature that was declared as an attacker or blocker at some point this turn. The
 * status persists after combat ends (so the creature still matches during a later main phase or
 * end step) and is cleared when the next turn begins. Used by Vizier of Deferment.
 */
public record PermanentAttackedOrBlockedThisTurnPredicate() implements PermanentPredicate {
}
