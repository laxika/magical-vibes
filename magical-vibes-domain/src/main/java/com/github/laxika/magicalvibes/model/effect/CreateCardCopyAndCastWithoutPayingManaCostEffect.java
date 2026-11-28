package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect.SpellbookCard;

import java.util.Objects;

/** Creates a copy of a card of {@code printing} and immediately casts it without paying its mana cost. */
public record CreateCardCopyAndCastWithoutPayingManaCostEffect(SpellbookCard printing) implements CardEffect {

    public CreateCardCopyAndCastWithoutPayingManaCostEffect {
        Objects.requireNonNull(printing, "printing");
    }
}
