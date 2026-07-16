package com.github.laxika.magicalvibes.networking.message;

import java.util.List;

public record BrowseCardInfo(
        String name,
        String collectorNumber,
        String setCode,
        String manaCost,
        String typeLine,
        String rarity,
        String power,
        String toughness,
        String color,
        List<String> colors,
        boolean implemented,
        String cardText,
        List<String> keywords,
        String type,
        List<String> additionalTypes,
        List<String> supertypes,
        List<String> subtypes,
        Integer loyalty,
        BrowseCardInfo backFace
) {
}
