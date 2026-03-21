package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record CreateCreatureTokenEffect(
        int amount,
        String tokenName,
        int power,
        int toughness,
        CardColor color,
        Set<CardColor> colors,
        List<CardSubtype> subtypes,
        Set<Keyword> keywords,
        Set<CardType> additionalTypes,
        boolean tappedAndAttacking,
        boolean tapped,
        Map<EffectSlot, CardEffect> tokenEffects,
        boolean exileAtEndOfCombat,
        boolean exileAtEndStep,
        boolean legendary
) implements CardEffect {

    /** Single-color token (existing pattern) */
    public CreateCreatureTokenEffect(String tokenName, int power, int toughness,
                                     CardColor color, List<CardSubtype> subtypes,
                                     Set<Keyword> keywords, Set<CardType> additionalTypes) {
        this(1, tokenName, power, toughness, color, null, subtypes, keywords, additionalTypes, false, false, Map.of(), false, false, false);
    }

    /** Single-color token with amount */
    public CreateCreatureTokenEffect(int amount, String tokenName, int power, int toughness,
                                     CardColor color, List<CardSubtype> subtypes,
                                     Set<Keyword> keywords, Set<CardType> additionalTypes) {
        this(amount, tokenName, power, toughness, color, null, subtypes, keywords, additionalTypes, false, false, Map.of(), false, false, false);
    }

    /** Multi-color token */
    public CreateCreatureTokenEffect(int amount, String tokenName, int power, int toughness,
                                     CardColor color, Set<CardColor> colors,
                                     List<CardSubtype> subtypes) {
        this(amount, tokenName, power, toughness, color, colors, subtypes, Set.of(), Set.of(), false, false, Map.of(), false, false, false);
    }

    /** Multi-color token (single) */
    public CreateCreatureTokenEffect(String tokenName, int power, int toughness,
                                     CardColor color, Set<CardColor> colors,
                                     List<CardSubtype> subtypes) {
        this(1, tokenName, power, toughness, color, colors, subtypes, Set.of(), Set.of(), false, false, Map.of(), false, false, false);
    }

    /** Single-color token, tapped and attacking */
    public CreateCreatureTokenEffect(int amount, String tokenName, int power, int toughness,
                                     CardColor color, List<CardSubtype> subtypes,
                                     boolean tappedAndAttacking) {
        this(amount, tokenName, power, toughness, color, null, subtypes, Set.of(), Set.of(), tappedAndAttacking, false, Map.of(), false, false, false);
    }

    /** Single-color token with keywords, tapped and attacking, exile at end of combat */
    public CreateCreatureTokenEffect(int amount, String tokenName, int power, int toughness,
                                     CardColor color, List<CardSubtype> subtypes,
                                     Set<Keyword> keywords, boolean tappedAndAttacking,
                                     boolean exileAtEndOfCombat) {
        this(amount, tokenName, power, toughness, color, null, subtypes, keywords, Set.of(), tappedAndAttacking, false, Map.of(), exileAtEndOfCombat, false, false);
    }

    /** Single-color token with amount and token effects */
    public CreateCreatureTokenEffect(int amount, String tokenName, int power, int toughness,
                                     CardColor color, List<CardSubtype> subtypes,
                                     Set<Keyword> keywords, Set<CardType> additionalTypes,
                                     Map<EffectSlot, CardEffect> tokenEffects) {
        this(amount, tokenName, power, toughness, color, null, subtypes, keywords, additionalTypes, false, false, tokenEffects, false, false, false);
    }

    /** Single-color token, enters tapped (not attacking) */
    public CreateCreatureTokenEffect(int amount, String tokenName, int power, int toughness,
                                     CardColor color, List<CardSubtype> subtypes,
                                     Set<Keyword> keywords, Set<CardType> additionalTypes,
                                     boolean tapped) {
        this(amount, tokenName, power, toughness, color, null, subtypes, keywords, additionalTypes, false, tapped, Map.of(), false, false, false);
    }
}
