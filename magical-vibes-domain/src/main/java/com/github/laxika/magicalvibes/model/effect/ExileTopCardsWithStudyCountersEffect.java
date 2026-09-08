package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Exiles the top number of cards from the controller's library and gives each exiled card a study
 * counter.
 */
public record ExileTopCardsWithStudyCountersEffect(DynamicAmount count) implements CardEffect {
}
