package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect (global, symmetric): each player who has already cast a nonartifact spell this turn
 * can't cast additional nonartifact spells. Artifact spells are never restricted, and the first
 * nonartifact spell of the turn is always allowed.
 *
 * <p>Ethersworn Canonist. Applies to every player, including the source's controller. Enforced in
 * {@code CastingPermissionService.isAdditionalNonartifactSpellRestricted}.
 */
public record CantCastAdditionalNonartifactSpellsEffect() implements CardEffect {
}
