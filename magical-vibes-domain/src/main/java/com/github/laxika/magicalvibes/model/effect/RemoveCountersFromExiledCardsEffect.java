package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.StepKind;

import java.util.Objects;

/**
 * "Remove a [kind] counter from it" of cards in exile: each card {@code cards} names that is in exile loses up to
 * {@code count} {@code counter} counters from the one store for counters on exiled cards
 * ({@code GameData.exiledCardCounters}). A REMOVE_COUNTERS step ({@link StepKind#REMOVE_COUNTERS}): it records the
 * number of counters removed, the cards that lost one and their owners, so "if you do" is
 * {@code HappenedThisWay.removedCounters()}. It never prompts.
 *
 * <p>Time counters come off one at a time as suspend's removal does it, each a time-counter-removed-in-exile event and
 * the last one offering a suspended card's cast (CR 702.62a), the effect's controller removing them.</p>
 *
 * <p>Mari, the Killing Quill's "remove a hit counter from a card that player owns in exile" (over the card a
 * {@code ChooseCardEffect} chose), All Hallow's Eve's "remove a scream counter from it" (over itself in exile), Dust of
 * Moments' "remove two time counters from each suspended card" and Talon Gates' "from each suspended card you own"
 * (over {@code exiledCards([you(),] CardIsSuspendedPredicate)}).</p>
 *
 * @param cards   which cards; never a target
 * @param counter the kind of counter
 * @param count   how many each card loses at most
 */
public record RemoveCountersFromExiledCardsEffect(CardSubject cards, CounterType counter, int count)
        implements CardEffect {

    public RemoveCountersFromExiledCardsEffect {
        Objects.requireNonNull(cards, "cards");
        Objects.requireNonNull(counter, "counter");
        if (cards.declaresTarget()) {
            throw new IllegalArgumentException("The step names cards without targeting them: " + cards);
        }
        if (count < 1) {
            throw new IllegalArgumentException("At least one counter: " + count);
        }
    }

    /** "Remove a [counter] counter from [cards]". */
    public RemoveCountersFromExiledCardsEffect(CardSubject cards, CounterType counter) {
        this(cards, counter, 1);
    }
}
