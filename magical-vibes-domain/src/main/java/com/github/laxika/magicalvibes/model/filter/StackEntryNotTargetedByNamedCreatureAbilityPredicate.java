package com.github.laxika.magicalvibes.model.filter;

public record StackEntryNotTargetedByNamedCreatureAbilityPredicate(String creatureName)
        implements StackEntryPredicate {
}
