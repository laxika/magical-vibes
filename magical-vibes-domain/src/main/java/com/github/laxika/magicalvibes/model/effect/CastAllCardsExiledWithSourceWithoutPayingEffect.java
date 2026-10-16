package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Offers the controller any number of the matching cards exiled with the source permanent to cast
 * without paying their mana costs while this ability resolves (Izzet Chemister).
 */
public record CastAllCardsExiledWithSourceWithoutPayingEffect(CardPredicate filter) implements CardEffect {
}
