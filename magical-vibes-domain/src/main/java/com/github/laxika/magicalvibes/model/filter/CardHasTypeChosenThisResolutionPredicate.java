package com.github.laxika.magicalvibes.model.filter;

/**
 * A card "of the chosen type" after "choose a creature type" in the same resolution
 * ({@code ChooseCreatureTypeAtResolutionEffect}), a card with changeling included (CR 702.73a); nothing while none is
 * chosen.
 */
public record CardHasTypeChosenThisResolutionPredicate() implements CardPredicate {
}
