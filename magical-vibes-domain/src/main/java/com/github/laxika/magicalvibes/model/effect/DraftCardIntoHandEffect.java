package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Puts a fresh card into its controller's hand because it was drafted from a spellbook; it was not
 * conjured, so "whenever you conjure" abilities don't trigger.
 */
public record DraftCardIntoHandEffect(Supplier<? extends Card> cardFactory) implements CardEffect {

    public DraftCardIntoHandEffect {
        Objects.requireNonNull(cardFactory, "Card factory cannot be null");
    }
}
