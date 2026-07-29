package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches permanents carrying the subtype chosen as the source permanent entered the battlefield
 * (the {@code chosenSubtype} recorded by {@code ChooseBasicLandTypeOnEnterEffect} and friends).
 * Source-dependent: matches nothing when the source is gone or made no choice.
 * Used by Shimmer ("each land of the chosen type has phasing").
 */
public record PermanentHasSourceChosenSubtypePredicate() implements PermanentPredicate {
}
