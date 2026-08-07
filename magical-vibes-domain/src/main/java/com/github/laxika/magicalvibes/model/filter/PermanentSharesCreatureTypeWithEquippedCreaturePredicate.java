package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches permanents that share at least one creature type with the creature the source Equipment
 * is currently attached to (Konda's Banner). Changeling counts as every creature type; a permanent
 * with no creature types shares none. Never matches while the Equipment is unattached. Needs game
 * data and the source's identity to evaluate.
 */
public record PermanentSharesCreatureTypeWithEquippedCreaturePredicate() implements PermanentPredicate {
}
