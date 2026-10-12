package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Exiles the top {@code count} cards of the controller's library, then lets the controller play
 * one of those cards until the end of the turn.
 *
 * @param count the number of cards to exile
 * @param chooseOnExile {@code true} when the controller must choose the one playable card as the
 *                      effect resolves ("Choose one. You may play that card this turn."); {@code false}
 *                      when every exiled card is playable but only one of them may be played ("You may
 *                      play one of those cards this turn.")
 */
public record ExileTopCardsChooseOneMayPlayThisTurnEffect(DynamicAmount count, boolean chooseOnExile)
        implements CardEffect {

    public ExileTopCardsChooseOneMayPlayThisTurnEffect(DynamicAmount count) {
        this(count, true);
    }

    public ExileTopCardsChooseOneMayPlayThisTurnEffect(int count) {
        this(new Fixed(count), true);
    }

    public static ExileTopCardsChooseOneMayPlayThisTurnEffect mayPlayOneOfThem(int count) {
        return new ExileTopCardsChooseOneMayPlayThisTurnEffect(new Fixed(count), false);
    }
}
