package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.StepKind;

import java.util.Objects;

/**
 * "Exile it with N time counters on it. If it doesn't have suspend, it gains suspend": each card {@link #cards()} names
 * that is in exile gets {@code timeCounters} time counters and is suspended (CR 702.62b), so suspend's upkeep and
 * last-counter triggers apply to it. The exile is the step before it, read as {@code Subjects.cardsThisWay(EXILE)}:
 * "exile this card from your graveyard" (Epochrasite, Sinister Concierge), "exile target creature" (Suspend), "exile
 * target nonland card from your graveyard" (Doom's Time Platform). A card the subject finds in another zone, or a token,
 * gets none.
 *
 * @param cards        the cards that get the counters
 * @param timeCounters        how many time counters each gets; 0 when it is each card's own mana value
 * @param equalToEachManaValue "a number of time counters on it equal to its mana value"; a card of mana value 0 gets
 *                            none, so it isn't suspended (CR 702.62b)
 */
public record PutTimeCountersAndGainSuspendEffect(CardSubject cards, int timeCounters, boolean equalToEachManaValue)
        implements CardEffect {

    public PutTimeCountersAndGainSuspendEffect {
        Objects.requireNonNull(cards, "cards");
        if (equalToEachManaValue ? timeCounters != 0 : timeCounters < 1) {
            throw new IllegalArgumentException("timeCounters must be positive, or each card's mana value");
        }
    }

    public PutTimeCountersAndGainSuspendEffect(CardSubject cards, int timeCounters) {
        this(cards, timeCounters, false);
    }

    /** "With a number of time counters on it equal to its mana value", on the cards the exile step exiled. */
    public static PutTimeCountersAndGainSuspendEffect equalToManaValueOnCardsExiledThisWay() {
        return new PutTimeCountersAndGainSuspendEffect(Subjects.cardsThisWay(StepKind.EXILE), 0, true);
    }

    /** The cards the preceding exile step of the same resolution exiled. */
    public static PutTimeCountersAndGainSuspendEffect onCardsExiledThisWay(int timeCounters) {
        return new PutTimeCountersAndGainSuspendEffect(Subjects.cardsThisWay(StepKind.EXILE), timeCounters);
    }
}
