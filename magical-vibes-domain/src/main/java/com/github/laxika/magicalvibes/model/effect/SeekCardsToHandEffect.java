package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Randomly seeks up to the evaluated number of matching cards from the controller's library to hand, recording
 * {@code StepKind.SEEK}.
 *
 * @param addsToEarlierSeek "seek a [X] and a [Y]": this seek is the second half of one instruction, so its cards join
 *                          the SEEK result of the seek before it in the resolution instead of replacing it ("discard
 *                          those cards" then names both); built by {@link #alsoSought(DynamicAmount, CardPredicate)}
 */
public record SeekCardsToHandEffect(DynamicAmount amount, CardPredicate filter, ManaValueBound manaValueBound,
                                    boolean addsToEarlierSeek)
        implements CardEffect {

    public SeekCardsToHandEffect(DynamicAmount amount, CardPredicate filter, ManaValueBound manaValueBound) {
        this(amount, filter, manaValueBound, false);
    }

    /** "... and a [filter] card": the second seek of one instruction, its cards added to the earlier seek's. */
    public static SeekCardsToHandEffect alsoSought(DynamicAmount amount, CardPredicate filter) {
        return new SeekCardsToHandEffect(amount, filter, null, true);
    }

    public SeekCardsToHandEffect(DynamicAmount amount, CardPredicate filter) {
        this(amount, filter, null);
    }

    public SeekCardsToHandEffect(CardPredicate filter) {
        this(new EventValue(), filter, null);
    }

    @Override
    public boolean referencesEventValue() {
        return amount instanceof EventValue
                || manaValueBound != null && manaValueBound.amount() instanceof EventValue;
    }
}
