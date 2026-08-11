package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Card;

import java.util.Set;

/**
 * Static marker effect: "You may cast [types] from the top of your library."
 * While a permanent with this effect is on the battlefield, the controller may
 * cast spells of the specified types from the top of their library (paying their
 * mana cost normally). The optional colorless clause is separate from the type
 * clause because colorless is a characteristic, not a card type.
 */
public record AllowCastFromTopOfLibraryEffect(Set<CardType> castableTypes, boolean castableColorless)
        implements CardEffect {

    public AllowCastFromTopOfLibraryEffect(Set<CardType> castableTypes) {
        this(castableTypes, false);
    }

    public boolean matches(Card card) {
        if (card.getType() == CardType.LAND) return false;
        boolean matchesType = castableTypes.contains(card.getType())
                || card.getAdditionalTypes().stream().anyMatch(castableTypes::contains);
        boolean matchesColorless = castableColorless
                && (card.getColors() == null || card.getColors().isEmpty());
        return matchesType || matchesColorless;
    }
}
