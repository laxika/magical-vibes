package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Mills cards, perpetually boosts creature cards in the controller's graveyard, then offers one matching milled card for return to hand. */
public record MillControllerThenPerpetuallyBoostGraveyardAndMayReturnMilledCardEffect(
        int count,
        CardPredicate returnFilter,
        int powerBoost,
        int toughnessBoost
) implements CardEffect {
}
