package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Reveals cards from the controller's library until the evaluated number of nonland cards is
 * revealed. The controller may exile one revealed card for each color among their permanents,
 * then the remaining revealed cards are shuffled back into the library and the exiled cards may
 * be cast this turn.
 */
public record RevealTopCardsForVividEffect(DynamicAmount nonlandCount) implements CardEffect {
}
