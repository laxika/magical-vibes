package com.github.laxika.magicalvibes.model;

import java.util.List;
import java.util.Set;

/**
 * @param colors        the face's rules colours (CR 202.2 — the colours of the mana symbols in its
 *                      mana cost, or its colour indicator). A land is colourless here.
 * @param colorIdentity the whole card's colour identity. Display-only: it is what tints a land's
 *                      frame in the UI, and it is never a rules characteristic — no colour
 *                      predicate may read it.
 * @param splitHalves   each half of a split card, in printed order, with only that half's own
 *                      characteristics (CR 709.3b). Empty for every other card.
 */
public record OracleData(
        String name,
        CardType type,
        Set<CardType> additionalTypes,
        String manaCost,
        CardColor color,
        List<CardColor> colors,
        List<CardColor> colorIdentity,
        Set<CardSupertype> supertypes,
        List<CardSubtype> subtypes,
        String cardText,
        Integer power,
        Integer toughness,
        Set<Keyword> keywords,
        Integer loyalty,
        Integer defense,
        String watermark,
        Integer combinedManaValue,
        List<OracleData> splitHalves
) {
    public OracleData {
        splitHalves = splitHalves == null ? List.of() : List.copyOf(splitHalves);
    }

    public OracleData(String name, CardType type, Set<CardType> additionalTypes, String manaCost,
                      CardColor color, List<CardColor> colors, List<CardColor> colorIdentity,
                      Set<CardSupertype> supertypes, List<CardSubtype> subtypes, String cardText,
                      Integer power, Integer toughness, Set<Keyword> keywords, Integer loyalty,
                      Integer defense, String watermark, Integer combinedManaValue) {
        this(name, type, additionalTypes, manaCost, color, colors, colorIdentity, supertypes,
                subtypes, cardText, power, toughness, keywords, loyalty, defense, watermark,
                combinedManaValue, List.of());
    }

    public OracleData(String name, CardType type, Set<CardType> additionalTypes, String manaCost,
                      CardColor color, List<CardColor> colors, List<CardColor> colorIdentity,
                      Set<CardSupertype> supertypes, List<CardSubtype> subtypes, String cardText,
                      Integer power, Integer toughness, Set<Keyword> keywords, Integer loyalty,
                      Integer defense, String watermark) {
        this(name, type, additionalTypes, manaCost, color, colors, colorIdentity, supertypes,
                subtypes, cardText, power, toughness, keywords, loyalty, defense, watermark, null);
    }

    /** This card's data with its split halves attached. */
    public OracleData withSplitHalves(List<OracleData> halves) {
        return new OracleData(name, type, additionalTypes, manaCost, color, colors, colorIdentity,
                supertypes, subtypes, cardText, power, toughness, keywords, loyalty, defense,
                watermark, combinedManaValue, halves);
    }

}
