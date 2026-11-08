package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a permanent the spell that triggered the resolving ability targeted as the ability triggered: "gain control
 * of those permanents" of "whenever you cast a spell that targets one or more permanents" (Dack Fayden's emblem),
 * "gain control of that creature" of "whenever you cast a spell that targets only a single creature" (Loki, God of
 * Lies). A target the spell changed to later isn't one of them. Reads the spell's targets from
 * {@link FilterContext#triggeringSpellTargetIds()}, which the resolving effect fills from its stack entry's triggering
 * spell.
 */
public record PermanentIsTargetOfTriggeringSpellPredicate() implements PermanentPredicate {
}
