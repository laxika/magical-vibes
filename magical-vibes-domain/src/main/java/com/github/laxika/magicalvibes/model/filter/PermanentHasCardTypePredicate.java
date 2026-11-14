package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.Objects;

/**
 * Matches a permanent, or a card judged as one (a card leaving a graveyard), that has {@code cardType} after the
 * layer system, for a card type with no predicate of its own ("an instant or sorcery card leaves your graveyard",
 * Murktide Regent).
 */
public record PermanentHasCardTypePredicate(CardType cardType) implements PermanentPredicate {

    public PermanentHasCardTypePredicate {
        Objects.requireNonNull(cardType, "cardType");
    }
}
