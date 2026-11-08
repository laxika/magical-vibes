package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

import java.util.List;
import java.util.Set;

/**
 * One-shot source effect that permanently changes the source into only a creature with the given
 * base power, base toughness, creature subtypes, additional keywords, and protection colors.
 * <p>
 * The base power and toughness are {@code power} / {@code toughness}, or, when {@code powerAmount} /
 * {@code toughnessAmount} is set, those amounts evaluated once as the effect resolves (CR 608.2h): "it becomes an
 * Illusion creature with power and toughness each equal to that spell's mana value" (Veiled Sentry,
 * {@code TargetSpellManaValue}).
 */
public record BecomeCreatureEffect(int power, int toughness, List<CardSubtype> subtypes,
                                   Set<Keyword> keywords, Set<CardColor> protectionFromColors, Set<CardType> additionalTypes,
                                   DynamicAmount powerAmount, DynamicAmount toughnessAmount) implements CardEffect {

    public BecomeCreatureEffect {
        subtypes = List.copyOf(subtypes);
        keywords = Set.copyOf(keywords);
        additionalTypes = Set.copyOf(additionalTypes);
        protectionFromColors = Set.copyOf(protectionFromColors);
    }

    public BecomeCreatureEffect(int power, int toughness, List<CardSubtype> subtypes,
                                Set<Keyword> keywords, Set<CardColor> protectionFromColors, Set<CardType> additionalTypes) {
        this(power, toughness, subtypes, keywords, protectionFromColors, additionalTypes, null, null);
    }

    public BecomeCreatureEffect(int power, int toughness, List<CardSubtype> subtypes,
                                Set<Keyword> keywords, Set<CardColor> protectionFromColors) {
        this(power, toughness, subtypes, keywords, protectionFromColors, Set.of());
    }

    public BecomeCreatureEffect(int power, int toughness, List<CardSubtype> subtypes) {
        this(power, toughness, subtypes, Set.of(), Set.of(), Set.of());
    }

    public BecomeCreatureEffect(int power, int toughness, List<CardSubtype> subtypes,
                                Set<Keyword> keywords) {
        this(power, toughness, subtypes, keywords, Set.of(), Set.of());
    }

    public BecomeCreatureEffect(int power, int toughness, CardSubtype subtype) {
        this(power, toughness, List.of(subtype), Set.of(), Set.of(), Set.of());
    }

    public BecomeCreatureEffect(int power, int toughness, CardSubtype subtype, Set<Keyword> keywords) {
        this(power, toughness, List.of(subtype), keywords, Set.of(), Set.of());
    }

    public BecomeCreatureEffect(int power, int toughness, CardSubtype subtype,
                                Set<Keyword> keywords, Set<CardColor> protectionFromColors) {
        this(power, toughness, List.of(subtype), keywords, protectionFromColors, Set.of());
    }

    /** The form whose base power and toughness are amounts evaluated as the effect resolves. */
    public BecomeCreatureEffect(DynamicAmount power, DynamicAmount toughness, CardSubtype subtype) {
        this(0, 0, List.of(subtype), Set.of(), Set.of(), Set.of(), power, toughness);
    }

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(null, false, null, true, 1);
    }
}
