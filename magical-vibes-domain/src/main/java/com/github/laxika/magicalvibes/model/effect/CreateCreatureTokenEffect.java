package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;
import java.util.Set;

public record CreateCreatureTokenEffect(
        int amount,
        String tokenName,
        int power,
        int toughness,
        CardColor color,
        Set<CardColor> colors,
        List<CardSubtype> subtypes,
        Set<Keyword> keywords,
        Set<CardType> additionalTypes,
        boolean tappedAndAttacking
) implements CardEffect {

    /** Single-color token (existing pattern) */
    public CreateCreatureTokenEffect(String tokenName, int power, int toughness,
                                     CardColor color, List<CardSubtype> subtypes,
                                     Set<Keyword> keywords, Set<CardType> additionalTypes) {
        this(1, tokenName, power, toughness, color, null, subtypes, keywords, additionalTypes, false);
    }

    /** Single-color token with amount */
    public CreateCreatureTokenEffect(int amount, String tokenName, int power, int toughness,
                                     CardColor color, List<CardSubtype> subtypes,
                                     Set<Keyword> keywords, Set<CardType> additionalTypes) {
        this(amount, tokenName, power, toughness, color, null, subtypes, keywords, additionalTypes, false);
    }

    /** Multi-color token */
    public CreateCreatureTokenEffect(int amount, String tokenName, int power, int toughness,
                                     CardColor color, Set<CardColor> colors,
                                     List<CardSubtype> subtypes) {
        this(amount, tokenName, power, toughness, color, colors, subtypes, Set.of(), Set.of(), false);
    }

    /** Multi-color token (single) */
    public CreateCreatureTokenEffect(String tokenName, int power, int toughness,
                                     CardColor color, Set<CardColor> colors,
                                     List<CardSubtype> subtypes) {
        this(1, tokenName, power, toughness, color, colors, subtypes, Set.of(), Set.of(), false);
    }

    /** Single-color token, tapped and attacking */
    public CreateCreatureTokenEffect(int amount, String tokenName, int power, int toughness,
                                     CardColor color, List<CardSubtype> subtypes,
                                     boolean tappedAndAttacking) {
        this(amount, tokenName, power, toughness, color, null, subtypes, Set.of(), Set.of(), tappedAndAttacking);
    }
}
