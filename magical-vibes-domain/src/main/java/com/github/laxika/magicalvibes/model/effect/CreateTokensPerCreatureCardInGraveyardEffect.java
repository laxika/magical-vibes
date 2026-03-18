package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;
import java.util.Set;

/**
 * Creates one creature token for each creature card in the controller's graveyard.
 * Tokens can optionally enter tapped and attacking.
 * Used by Kessig Cagebreakers (ISD #189).
 */
public record CreateTokensPerCreatureCardInGraveyardEffect(
        String tokenName,
        int power,
        int toughness,
        CardColor color,
        List<CardSubtype> subtypes,
        Set<Keyword> keywords,
        Set<CardType> additionalTypes,
        boolean tappedAndAttacking
) implements CardEffect {

    /** Convenience constructor for tapped-and-attacking tokens with no keywords or additional types */
    public CreateTokensPerCreatureCardInGraveyardEffect(String tokenName, int power, int toughness,
                                                         CardColor color, List<CardSubtype> subtypes,
                                                         boolean tappedAndAttacking) {
        this(tokenName, power, toughness, color, subtypes, Set.of(), Set.of(), tappedAndAttacking);
    }
}
