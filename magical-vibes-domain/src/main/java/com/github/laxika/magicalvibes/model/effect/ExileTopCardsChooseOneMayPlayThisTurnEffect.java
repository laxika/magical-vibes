package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Exiles the top {@code count} cards of the controller's library, then has the controller choose
 * one of those cards to play until the end of the turn.
 */
public record ExileTopCardsChooseOneMayPlayThisTurnEffect(DynamicAmount count) implements CardEffect {

    public ExileTopCardsChooseOneMayPlayThisTurnEffect(int count) {
        this(new Fixed(count));
    }
}
