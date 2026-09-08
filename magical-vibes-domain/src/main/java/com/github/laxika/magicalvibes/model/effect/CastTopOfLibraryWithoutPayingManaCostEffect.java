package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;

import java.util.Set;

/**
 * Looks at the top card of the library and may cast it without paying its mana cost
 * if it matches one of the specified card types.
 * Used by Galvanoth ("you may look at the top card... you may cast it without paying
 * its mana cost if it's an instant or sorcery spell").
 */
public record CastTopOfLibraryWithoutPayingManaCostEffect(Set<CardType> castableTypes) implements CardEffect {

    public boolean matches(Card card) {
        if (card.hasType(CardType.LAND)) {
            return false;
        }
        return castableTypes.contains(card.getType())
                || card.getAdditionalTypes().stream().anyMatch(castableTypes::contains);
    }
}
