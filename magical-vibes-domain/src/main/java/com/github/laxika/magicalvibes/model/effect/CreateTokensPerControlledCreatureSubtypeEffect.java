package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;
import java.util.Set;

/**
 * Creates creature tokens based on the number of creatures with a specific subtype the controller controls,
 * divided by a divisor (rounded down). The count is determined at resolution time.
 * Used for cards like Endless Ranks of the Dead ("create X 2/2 black Zombie creature tokens,
 * where X is half the number of Zombies you control, rounded down").
 */
public record CreateTokensPerControlledCreatureSubtypeEffect(
        CardSubtype subtype,
        int divisor,
        String tokenName,
        int power,
        int toughness,
        CardColor color,
        List<CardSubtype> subtypes,
        Set<Keyword> keywords,
        Set<CardType> additionalTypes
) implements CardEffect {
}
