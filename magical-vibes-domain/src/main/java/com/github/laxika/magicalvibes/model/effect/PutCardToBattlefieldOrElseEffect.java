package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Puts a matching card from hand onto the battlefield, or resolves the fallback when no matching
 * card is available. The card choice itself remains declinable when this effect is wrapped in a
 * {@link MayEffect}.
 */
public record PutCardToBattlefieldOrElseEffect(CardPredicate predicate, String label, CardEffect elseEffect)
        implements CardEffect {
}
