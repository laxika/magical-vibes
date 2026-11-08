package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches permanents that share a creature type with the permanent the triggered ability's event was about: "creatures
 * that share a creature type with it" (Faces of the Past). Read from {@link FilterContext#triggeringPermanentId()}
 * while that permanent is on the battlefield and from {@link FilterContext#triggeringPermanentSnapshot()}, its
 * last-known information, once it has left (CR 603.10a, 608.2h). Changeling counts as every creature type; with no
 * triggering permanent it matches nothing.
 */
public record PermanentSharesCreatureTypeWithTriggeringPermanentPredicate() implements PermanentPredicate {
}
