package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals a card from the controller's hand, then searches their library for one card with the
 * revealed card's name, revealing it and putting it into hand. If the controller has no cards in
 * hand, the search is unrestricted instead.
 */
public record SearchLibraryForCardWithSameNameAsCardInHandOrAnyIfEmptyEffect() implements CardEffect {
}
