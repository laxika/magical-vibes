package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Static self-effect: the source gains all activated abilities of cards matching the filter in
 * its controller's graveyard.
 */
public record GainActivatedAbilitiesOfCardsInControllerGraveyardEffect(CardPredicate filter)
        implements CardEffect {
}
