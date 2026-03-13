package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;
import java.util.Set;

/**
 * Creates creature tokens equal to the number of lands with a specific subtype the controller controls.
 * The count is determined at resolution time.
 * Used for cards like Howl of the Night Pack ("create a 2/2 green Wolf creature token for each Forest you control").
 */
public record CreateTokensPerControlledLandSubtypeEffect(
        CardSubtype landSubtype,
        String tokenName,
        int power,
        int toughness,
        CardColor color,
        List<CardSubtype> subtypes,
        Set<Keyword> keywords,
        Set<CardType> additionalTypes
) implements CardEffect {
}
