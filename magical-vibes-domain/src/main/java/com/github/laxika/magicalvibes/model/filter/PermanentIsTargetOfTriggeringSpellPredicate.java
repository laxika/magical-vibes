package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a permanent the spell that triggered the resolving ability targets: "gain control of those permanents" of
 * "whenever you cast a spell that targets one or more permanents" (Dack Fayden's emblem). Reads the spell's targets
 * from {@link FilterContext#triggeringSpellTargetIds()}, which the resolving effect fills from its stack entry.
 */
public record PermanentIsTargetOfTriggeringSpellPredicate() implements PermanentPredicate {
}
