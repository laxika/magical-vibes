package com.github.laxika.magicalvibes.model.amount;

/**
 * The number of non-token cards put into the controller's graveyard from hand or library this
 * turn. Cards are counted by identity, so a card that changes zones after entering the graveyard
 * remains counted for the rest of the turn.
 */
public record CardsPutIntoGraveyardFromHandOrLibraryThisTurn() implements DynamicAmount {
}
