package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches permanents whose effective power is at most the power of the permanent the ability is about (the stack
 * entry's triggering permanent, {@link FilterContext#triggeringPermanentId()}), read each time the predicate is
 * evaluated: the permanent's power as it is, or as it last existed once it has left the battlefield (CR 608.2h).
 * Without a triggering permanent it matches nothing. Used by Nihiloor's "power less than or equal to the tapped
 * creature's power", which is checked both when the reflexive ability is put on the stack and again when it resolves.
 */
public record PermanentPowerAtMostTriggeringPermanentPowerPredicate() implements PermanentPredicate {
}
