package com.github.laxika.magicalvibes.model.effect;

import java.util.ArrayList;
import java.util.List;

/**
 * Conjures a full, non-token card by printing or by name onto the controller's battlefield.
 *
 * <p>{@code conjureEventCardCount} is the number of cards in the conjure event this card completes,
 * passed to "whenever you conjure" triggers. Effects that conjure several cards at once queue one
 * instance per card and give the count only to the last one; the others use {@code 0} so the event
 * triggers once.
 */
public record ConjureCardToBattlefieldEffect(String setCode, String collectorNumber, String cardName,
                                             int conjureEventCardCount)
        implements CardEffect {

    public ConjureCardToBattlefieldEffect(String setCode, String collectorNumber) {
        this(setCode, collectorNumber, null, 1);
    }

    public ConjureCardToBattlefieldEffect(String cardName) {
        this(null, null, cardName, 1);
    }

    /** One card of a multi-card conjure event; see {@link #conjureEventCardCount()}. */
    public static ConjureCardToBattlefieldEffect partOfEvent(String cardName, int conjureEventCardCount) {
        return new ConjureCardToBattlefieldEffect(null, null, cardName, conjureEventCardCount);
    }

    /** Queues one conjure per name as a single conjure event. */
    public static List<CardEffect> asOneEvent(List<String> cardNames) {
        List<CardEffect> conjures = new ArrayList<>(cardNames.size());
        for (int i = 0; i < cardNames.size(); i++) {
            conjures.add(partOfEvent(cardNames.get(i), i == cardNames.size() - 1 ? cardNames.size() : 0));
        }
        return conjures;
    }
}
