package com.github.laxika.magicalvibes.model.filter;

/**
 * "Of the chosen type" after "choose a creature type" in the same resolution
 * ({@code ChooseCreatureTypeAtResolutionEffect}): matches a permanent with that type, and nothing while none is
 * chosen.
 */
public record PermanentHasTypeChosenThisResolutionPredicate() implements PermanentPredicate {
}
