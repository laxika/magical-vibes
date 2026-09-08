package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches the permanent the source Aura or Equipment is currently attached to.
 * Needs game data and the source card id to evaluate. Wrap in {@link PermanentNotPredicate} for
 * the common "target creature other than enchanted creature" restriction (Kjeldoran Pride).
 */
public record PermanentIsHostOfSourceAuraPredicate() implements PermanentPredicate {
}
