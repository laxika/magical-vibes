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
        /**
         * The guild or faction mark printed behind the rules text, as Scryfall spells it, or
         * {@code null} on the great majority of cards, which have none. Read from the top-level
         * card rather than a face: a double-faced card carries one watermark for both sides.
         */
        String watermark,
        BrowseCardInfo backFace,
        /**
         * The spell printed inset on a prepare card's front face. Mutually exclusive with
         * {@code backFace}: a prepare card's second face is drawn alongside the front, not
         * flipped to, so it must not be offered as a back face.
         */
        BrowseCardInfo prepareSpell,
        /**
         * Scryfall {@code promo_types} for this printing (e.g. {@code planeswalkerdeck},
         * {@code setextension}). Empty for ordinary base-set cards. Used by the deck editor to
         * group exclusives under their own headers.
         */
        List<String> promoTypes
) {
}
