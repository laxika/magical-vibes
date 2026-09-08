package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * The controller chooses an opponent, then that opponent chooses a matching card from the
 * controller's graveyard and returns it to the controller's hand.
 */
public record OpponentChoosesCardFromGraveyardToHandEffect(CardPredicate filter) implements CardEffect {
}
