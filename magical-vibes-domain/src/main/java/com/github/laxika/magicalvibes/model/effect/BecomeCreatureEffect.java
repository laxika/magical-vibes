package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;
import java.util.Set;

/**
 * One-shot source effect that permanently changes the source into a creature with the given base
 * power, base toughness, creature subtypes, additional card types, and keywords. The supplied card
 * types replace the source's previous non-creature card types.
 */
public record BecomeCreatureEffect(int power, int toughness, List<CardSubtype> subtypes,
                                   Set<Keyword> keywords, Set<CardType> additionalTypes) implements CardEffect {

    public BecomeCreatureEffect {
        subtypes = List.copyOf(subtypes);
        keywords = Set.copyOf(keywords);
        additionalTypes = Set.copyOf(additionalTypes);
    }

    public BecomeCreatureEffect(int power, int toughness, List<CardSubtype> subtypes) {
        this(power, toughness, subtypes, Set.of(), Set.of());
    }

    public BecomeCreatureEffect(int power, int toughness, List<CardSubtype> subtypes,
                                Set<Keyword> keywords) {
        this(power, toughness, subtypes, keywords, Set.of());
    }

    public BecomeCreatureEffect(int power, int toughness, CardSubtype subtype) {
        this(power, toughness, List.of(subtype), Set.of(), Set.of());
    }

    public BecomeCreatureEffect(int power, int toughness, CardSubtype subtype, Set<Keyword> keywords) {
        this(power, toughness, List.of(subtype), keywords, Set.of());
    }

    public BecomeCreatureEffect(int power, int toughness, CardSubtype subtype,
                                Set<Keyword> keywords, Set<CardType> additionalTypes) {
        this(power, toughness, List.of(subtype), keywords, additionalTypes);
    }

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(null, false, null, true, 1);
    }
}
