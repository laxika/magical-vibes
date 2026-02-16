package com.github.laxika.magicalvibes.model;

import java.util.List;
import java.util.Set;

public record OracleData(
        String name,
        CardType type,
        String manaCost,
        CardColor color,
        Set<CardSupertype> supertypes,
        List<CardSubtype> subtypes,
        String cardText,
        Integer power,
        Integer toughness,
        Set<Keyword> keywords
) {}
