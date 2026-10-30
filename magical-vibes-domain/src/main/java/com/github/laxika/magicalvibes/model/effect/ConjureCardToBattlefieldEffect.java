package com.github.laxika.magicalvibes.model.effect;

import java.util.ArrayList;
import java.util.List;

/**
 * Conjures a full, non-token card of the given printing onto the controller's battlefield.
 *
 * <p>{@code conjureEventCardCount} is the number of cards in the conjure event this card completes,
 * passed to "whenever you conjure" triggers. Effects that conjure several cards at once queue one
 * instance per card and give the count only to the last one; the others use {@code 0} so the event
 * triggers once.
 */
public record ConjureCardToBattlefieldEffect(String setCode, String collectorNumber, int conjureEventCardCount)
        implements CardEffect {

    public ConjureCardToBattlefieldEffect(String setCode, String collectorNumber) {
        this(setCode, collectorNumber, 1);
    }

    /** One card of a multi-card conjure event; see {@link #conjureEventCardCount()}. */
    public static ConjureCardToBattlefieldEffect partOfEvent(String setCode, String collectorNumber,
                                                             int conjureEventCardCount) {
        return new ConjureCardToBattlefieldEffect(setCode, collectorNumber, conjureEventCardCount);
    }

    /** Queues one conjure per printing as a single conjure event. */
    public static List<CardEffect> asOneEvent(List<DraftFromSpellbookEffect.SpellbookCard> printings) {
        List<CardEffect> conjures = new ArrayList<>(printings.size());
        for (int i = 0; i < printings.size(); i++) {
            DraftFromSpellbookEffect.SpellbookCard printing = printings.get(i);
            conjures.add(partOfEvent(printing.setCode(), printing.collectorNumber(),
                    i == printings.size() - 1 ? printings.size() : 0));
        }
        return conjures;
    }
}
