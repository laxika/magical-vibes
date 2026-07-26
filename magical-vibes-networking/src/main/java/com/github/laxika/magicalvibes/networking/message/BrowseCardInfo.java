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
        BrowseCardInfo backFace,
        /**
         * The spell printed inset on a prepare card's front face. Mutually exclusive with
         * {@code backFace}: a prepare card's second face is drawn alongside the front, not
         * flipped to, so it must not be offered as a back face.
         */
        BrowseCardInfo prepareSpell
) {
}
