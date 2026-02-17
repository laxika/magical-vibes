package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;
import java.util.Set;

public record CreateCreatureTokenEffect(
        String tokenName,
        int power,
        int toughness,
        CardColor color,
        List<CardSubtype> subtypes,
        Set<Keyword> keywords,
        Set<CardType> additionalTypes
) implements CardEffect {
}
