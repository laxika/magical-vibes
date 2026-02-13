package com.github.laxika.magicalvibes.networking.model;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;
import java.util.Set;

public record CardView(
        String name,
        CardType type,
        List<CardSubtype> subtypes,
        String cardText,
        String manaCost,
        Integer power,
        Integer toughness,
        Set<Keyword> keywords,
        boolean needsTarget,
        boolean hasTapAbility,
        boolean hasManaAbility,
        String setCode,
        String collectorNumber,
        String flavorText,
        CardColor color,
        List<CardType> allowedTargetTypes
) {
}
