package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches an Aura permanent that is currently attached to a land. Requires game data to
 * evaluate because the permanent it is attached to must be looked up and confirmed to be a land.
 */
public record PermanentIsAuraAttachedToLandPredicate() implements PermanentPredicate {
}
