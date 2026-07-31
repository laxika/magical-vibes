package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * The card imprinted on the source permanent matches {@code filter}. Used for abilities whose
 * effect looks back at a card their own cost set aside — e.g. Storm Elemental's "Exile the top
 * card of your library: If the exiled card is a snow land, ...", where the exile cost imprints
 * the card it exiled.
 *
 * @param description human-readable card description used in log messages ("a snow land")
 */
public record ImprintedCardMatches(CardPredicate filter, String description) implements Condition {

    @Override
    public String conditionName() {
        return "exiled card is " + description;
    }

    @Override
    public String conditionNotMetReason() {
        return "the exiled card is not " + description;
    }
}
