package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Each opponent chooses between discarding a card and letting the effect controller optionally
 * put one matching card from their hand onto the battlefield.
 */
public record EachOpponentFacesVillainousChoiceEffect(CardPredicate predicate, String label)
        implements CardEffect {
}
