package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.List;
import java.util.Set;

public record CreateCreatureTokenWithColorsEffect(
        String tokenName,
        int power,
        int toughness,
        Set<CardColor> colors,
        CardColor primaryColor,
        List<CardSubtype> subtypes
) implements CardEffect {
}
