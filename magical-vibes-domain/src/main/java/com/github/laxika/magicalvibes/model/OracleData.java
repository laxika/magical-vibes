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

    /**
     * The mana value of a card with this data, read without building the card. {@link Card} uses
     * the same rule, so the two cannot disagree.
     */
    public int manaValue() {
        return manaValue(combinedManaValue, manaCost == null ? null : new ManaCost(manaCost));
    }

    /** Whether a card with this data has the card type, read without building the card. */
    public boolean hasType(CardType cardType) {
        return hasType(type, additionalTypes, cardType);
    }

    /** The names a card with this data can be chosen by, read without building the card. */
    public List<String> names() {
        return names(name);
    }

    /**
     * The one naming rule, shared with {@link Card#getNames()} like
     * {@link #manaValue(Integer, ManaCost)}: each half's name for a split card, otherwise its one
     * name, and none without a name.
     */
    static List<String> names(String name) {
        if (name == null) {
            return List.of();
        }
        return name.contains(" // ") ? List.of(name.split(" // ")) : List.of(name);
    }

    /**
     * The one mana-value rule: a split card's combined value, otherwise that of its mana cost, and
     * 0 with no mana cost. Takes the characteristics rather than reading this record because
     * {@link Card} applies it to its own fields, which runtime effects can change.
     */
    static int manaValue(Integer combinedManaValue, ManaCost manaCost) {
        if (combinedManaValue != null) {
            return combinedManaValue;
        }
        return manaCost != null ? manaCost.getManaValue() : 0;
    }

    /** The one card-type rule, shared with {@link Card} like {@link #manaValue(Integer, ManaCost)}. */
    static boolean hasType(CardType type, Set<CardType> additionalTypes, CardType cardType) {
        return type == cardType || additionalTypes.contains(cardType);
    }

    /** This card's data with its split halves attached. */
    public OracleData withSplitHalves(List<OracleData> halves) {
        return new OracleData(name, type, additionalTypes, manaCost, color, colors, colorIdentity,
                supertypes, subtypes, cardText, power, toughness, keywords, loyalty, defense,
                watermark, combinedManaValue, halves);
    }

}
