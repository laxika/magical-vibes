package com.github.laxika.magicalvibes.model.filter;

/**
 * "A card with the chosen name" in the steps after a "choose a card name" step of the same resolution: one of the
 * card's names is the chosen one (CR 709.4a). Matches nothing when none was chosen.
 */
public record CardHasNameChosenThisResolutionPredicate() implements CardPredicate {
}
