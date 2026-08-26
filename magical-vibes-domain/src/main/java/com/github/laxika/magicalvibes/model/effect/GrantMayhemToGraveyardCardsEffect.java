package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Static effect that grants mayhem to matching cards in its controller's graveyard.
 */
public record GrantMayhemToGraveyardCardsEffect(CardPredicate filter) implements CardEffect {
}
