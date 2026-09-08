package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches an Aura currently attached to a creature or land. Evaluation requires game data
 * because both attachment state and the host's current types can change.
 */
public record PermanentIsAuraAttachedToCreatureOrLandPredicate() implements PermanentPredicate {
}
