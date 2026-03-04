package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;
import java.util.Set;

/**
 * Creates X creature tokens, where X comes from the spell's X value on the stack entry.
 * Used for spells like White Sun's Zenith ("Create X 2/2 white Cat creature tokens").
 */
public record CreateXCreatureTokenEffect(
        String tokenName,
        int power,
        int toughness,
        CardColor color,
        List<CardSubtype> subtypes,
        Set<Keyword> keywords,
        Set<CardType> additionalTypes
) implements CardEffect {

    /** Convenience constructor for tokens with no keywords or additional types */
    public CreateXCreatureTokenEffect(String tokenName, int power, int toughness,
                                       CardColor color, List<CardSubtype> subtypes) {
        this(tokenName, power, toughness, color, subtypes, Set.of(), Set.of());
    }
}
