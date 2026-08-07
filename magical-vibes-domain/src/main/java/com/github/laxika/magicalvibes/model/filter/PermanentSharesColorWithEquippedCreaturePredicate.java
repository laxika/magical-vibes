package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches permanents that share at least one color with the creature the source Equipment is
 * currently attached to (Konda's Banner). Never matches while the Equipment is unattached, and
 * never matches a colorless permanent or a colorless equipped creature — sharing a color requires
 * both sides to have one. Needs game data and the source's identity to evaluate.
 */
public record PermanentSharesColorWithEquippedCreaturePredicate() implements PermanentPredicate {
}
