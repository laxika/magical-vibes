package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

import java.util.List;
import java.util.Objects;

/** Conjures a random implemented card with the evaluated mana value from a spellbook. */
public record ConjureRandomCardFromSpellbookWithManaValueToBattlefieldEffect(
        List<String> cardNames, DynamicAmount manaValue) implements CardEffect {

    public ConjureRandomCardFromSpellbookWithManaValueToBattlefieldEffect {
        cardNames = List.copyOf(Objects.requireNonNull(cardNames, "Spellbook cannot be null"));
        Objects.requireNonNull(manaValue, "Mana value cannot be null");
    }
}
