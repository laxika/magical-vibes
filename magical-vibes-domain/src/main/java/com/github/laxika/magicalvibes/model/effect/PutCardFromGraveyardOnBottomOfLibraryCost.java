package com.github.laxika.magicalvibes.model.effect;

/**
 * Cost effect that requires putting one card from the controller's graveyard on the bottom of
 * its owner's library.
 *
 * <p>No card in the controller's graveyard makes the cost unpayable. When used as the payable
 * side of a {@link ForcedCostOrElseEffect}, accepting the payment opens a mandatory graveyard
 * choice.
 */
public record PutCardFromGraveyardOnBottomOfLibraryCost() implements CostEffect {
}
