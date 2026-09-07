package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * The controller chooses one matching card from each player's graveyard and puts those cards
 * onto the battlefield under the controller's control.
 *
 * @param filter predicate restricting the cards that may be chosen
 */
public record EachPlayerChoosesCardFromGraveyardToBattlefieldEffect(CardPredicate filter)
        implements CardEffect {
}
