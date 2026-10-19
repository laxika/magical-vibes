package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches any permanent while its controller has activated an ability of an artifact this turn.
 * Evaluated against the source controller of the filter context, so it reads player state rather
 * than the permanent being checked.
 */
public record ControllerActivatedArtifactAbilityThisTurnPredicate() implements PermanentPredicate {
}
