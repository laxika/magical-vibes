package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a stack entry that is not currently targeted by an activated or triggered ability from
 * another creature with the supplied name. The evaluating source permanent identifies which
 * creature is "another".
 */
public record StackEntryNotTargetedByAnotherNamedCreatureAbilityPredicate(String creatureName)
        implements StackEntryPredicate {
}
