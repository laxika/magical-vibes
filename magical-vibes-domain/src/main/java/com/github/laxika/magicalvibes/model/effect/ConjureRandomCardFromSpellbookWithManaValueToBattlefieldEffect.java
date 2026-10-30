package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

import java.util.List;
import java.util.Objects;

/** Conjures a random implemented card with the evaluated mana value from a spellbook of printings. */
public record ConjureRandomCardFromSpellbookWithManaValueToBattlefieldEffect(
        List<DraftFromSpellbookEffect.SpellbookCard> spellbook, DynamicAmount manaValue) implements CardEffect {

    public ConjureRandomCardFromSpellbookWithManaValueToBattlefieldEffect {
        spellbook = List.copyOf(Objects.requireNonNull(spellbook, "Spellbook cannot be null"));
        Objects.requireNonNull(manaValue, "Mana value cannot be null");
    }
}
