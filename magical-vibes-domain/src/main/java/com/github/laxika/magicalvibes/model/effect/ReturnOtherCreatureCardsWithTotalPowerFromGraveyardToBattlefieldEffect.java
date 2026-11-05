package com.github.laxika.magicalvibes.model.effect;

/**
 * "Return any number of other creature cards with total power X or less from your graveyard to the
 * battlefield, where X is this creature's power." Nothing is targeted: the controller chooses the cards as
 * the ability resolves. X is the dying creature's last known power, carried on the stack entry's event
 * value by the death-trigger collector. "Other" excludes the card that died, the stack entry's triggering card.
 */
public record ReturnOtherCreatureCardsWithTotalPowerFromGraveyardToBattlefieldEffect() implements CardEffect {
}
