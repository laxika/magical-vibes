package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches an Aura permanent currently attached to the source permanent, regardless of who controls
 * the Aura. Needs game data and the source card id to evaluate. Used by Hakim, Loreweaver
 * ("Destroy all Auras attached to Hakim").
 */
public record PermanentIsAuraAttachedToSourcePredicate() implements PermanentPredicate {
}
