package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Each opponent exiles a matching permanent they control and a random nonland card from their
 * library, then takes damage equal to the total mana value of the cards exiled for them.
 */
public record EachOpponentExilesNonlandNontokenPermanentAndRandomNonlandCardThenDealsManaValueDamageEffect(
        PermanentPredicate permanentFilter
) implements CardEffect {
}
