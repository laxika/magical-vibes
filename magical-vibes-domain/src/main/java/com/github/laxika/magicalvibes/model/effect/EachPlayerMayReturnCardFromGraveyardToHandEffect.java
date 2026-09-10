package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Queues a may choice for each player to return one matching card from their graveyard. */
public record EachPlayerMayReturnCardFromGraveyardToHandEffect(CardPredicate filter) implements CardEffect {
}
