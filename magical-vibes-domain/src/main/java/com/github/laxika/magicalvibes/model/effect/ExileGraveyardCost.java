package com.github.laxika.magicalvibes.model.effect;

/**
 * "Exile all cards from your graveyard" as a cost (Grip of Amnesia's ransom). It can be paid with an empty graveyard
 * (Grip of Amnesia's ruling), so it is always payable. Unlike {@link ExileAnyNumberOfCardsFromGraveyardCost}, the
 * payer chooses nothing: every card goes.
 */
public record ExileGraveyardCost() implements CostEffect {
}
