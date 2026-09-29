package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Trigger marker for creating Food when a matching card is played from the library top. */
public record CreateFoodWhenPlayingCardFromTopOfLibraryEffect(CardPredicate filter)
        implements CardEffect {
}
