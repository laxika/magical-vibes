package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Exiles the top {@code count} cards of the controller's library, then has the controller choose
 * one of those cards to play until the end of their next turn.
 *
 * @param count how many cards are exiled from the top of the library
 */
public record ExileTopCardsChooseOneMayPlayUntilNextTurnEffect(DynamicAmount count) implements CardEffect {

    public ExileTopCardsChooseOneMayPlayUntilNextTurnEffect(int count) {
        this(new Fixed(count));
    }
}
