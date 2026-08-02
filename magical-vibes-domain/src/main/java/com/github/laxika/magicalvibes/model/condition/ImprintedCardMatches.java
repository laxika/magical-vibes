package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * The card imprinted on the source permanent matches {@code filter}. Used for abilities whose
 * effect looks back at a card their own cost set aside — e.g. Storm Elemental's "Exile the top
 * card of your library: If the exiled card is a snow land, ...", where the exile cost imprints
 * the card it exiled.
 *
 * @param description human-readable card description used in log messages ("a snow land")
 * @param subject     how the imprinted card is referred to in log messages; "exiled card" by
 *                    default, "discarded card" for a discard cost that imprints what it paid with
 *                    (Necromancer's Stockpile)
 */
public record ImprintedCardMatches(CardPredicate filter, String description, String subject) implements Condition {

    public ImprintedCardMatches(CardPredicate filter, String description) {
        this(filter, description, "exiled card");
    }

    @Override
    public String conditionName() {
        return subject + " is " + description;
    }

    @Override
    public String conditionNotMetReason() {
        return "the " + subject + " is not " + description;
    }
}
