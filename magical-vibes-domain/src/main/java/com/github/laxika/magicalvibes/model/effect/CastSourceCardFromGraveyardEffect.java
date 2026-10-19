package com.github.laxika.magicalvibes.model.effect;

/**
 * Offers the resolving source card for a normal-cost cast from its controller's graveyard
 * (any cost reductions apply; the mana cost is paid when the offer is accepted).
 */
public record CastSourceCardFromGraveyardEffect() implements CardEffect {
}
