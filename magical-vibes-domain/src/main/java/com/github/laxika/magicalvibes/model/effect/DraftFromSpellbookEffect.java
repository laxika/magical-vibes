package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/** Offers a random subset of a spellbook and lets the controller choose one card. */
public record DraftFromSpellbookEffect(List<Supplier<? extends Card>> cardFactories, int offeredCardCount)
        implements CardEffect {

    public DraftFromSpellbookEffect {
        Objects.requireNonNull(cardFactories, "Spellbook cannot be null");
        cardFactories = List.copyOf(cardFactories);
        if (cardFactories.isEmpty()) {
            throw new IllegalArgumentException("Spellbook cannot be empty");
        }
        if (offeredCardCount < 1 || offeredCardCount > cardFactories.size()) {
            throw new IllegalArgumentException("Invalid number of offered spellbook cards");
        }
    }

    public DraftFromSpellbookEffect(List<Supplier<? extends Card>> cardFactories) {
        this(cardFactories, 3);
    }
}
