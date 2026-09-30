package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.Objects;

/** Conjures one random listed card onto the resolving controller's battlefield. */
public record ConjureRandomCardFromSpellbookToBattlefieldEffect(
        List<String> cardNames, int basePower, int baseToughness) implements CardEffect {

    public ConjureRandomCardFromSpellbookToBattlefieldEffect {
        cardNames = List.copyOf(Objects.requireNonNull(cardNames, "Spellbook cannot be null"));
        if (cardNames.isEmpty()) {
            throw new IllegalArgumentException("Spellbook must not be empty");
        }
    }
}
