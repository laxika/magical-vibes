package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches an Aura permanent that is currently attached to a creature. Requires game data to
 * evaluate (the permanent it is attached to must be looked up and confirmed to be a creature).
 * Used by Crown of the Ages ("Attach target Aura attached to a creature to another creature").
 */
public record PermanentIsAuraAttachedToCreaturePredicate() implements PermanentPredicate {
}
