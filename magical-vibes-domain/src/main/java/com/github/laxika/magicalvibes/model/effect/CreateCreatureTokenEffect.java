package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.List;

public record CreateCreatureTokenEffect(
        String tokenName,
        int power,
        int toughness,
        CardColor color,
        List<CardSubtype> subtypes
) implements CardEffect {
}
