package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Offers each matching instant or sorcery card in the controller's graveyard for a free cast.
 * The effect itself does not target; each offer is made as the ability resolves.
 */
public record CastMatchingInstantOrSorceryFromGraveyardWithoutPayingManaCostEffect(
        CardPredicate filter
) implements CardEffect {
}
