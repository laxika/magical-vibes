package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Offers one matching spell from the controller's hand, graveyard, or exile for free.
 * The same effect is used as the resolved emblem ability and as the pending-choice marker so
 * accepting one offer withdraws all remaining offers.
 */
public record MayCastLegendarySpellFromAnyZoneEffect(CardPredicate filter) implements CardEffect {
}
