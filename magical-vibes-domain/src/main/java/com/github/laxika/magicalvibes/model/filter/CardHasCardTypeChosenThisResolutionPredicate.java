package com.github.laxika.magicalvibes.model.filter;

/**
 * A card "of the chosen type" after "choose a card type" in the same resolution
 * ({@code ChooseCardTypeAtResolutionEffect}), judged by the card's types as effects leave them; nothing while none is
 * chosen.
 */
public record CardHasCardTypeChosenThisResolutionPredicate() implements CardPredicate {
}
