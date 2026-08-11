package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;
import java.util.Set;

/**
 * One-shot source effect that permanently changes the source into only a creature with the given
 * base power, base toughness, creature subtypes, and any additional keywords.
 */
public record BecomeCreatureEffect(int power, int toughness, List<CardSubtype> subtypes,
                                   Set<Keyword> keywords) implements CardEffect {

    public BecomeCreatureEffect {
        subtypes = List.copyOf(subtypes);
        keywords = Set.copyOf(keywords);
    }

    public BecomeCreatureEffect(int power, int toughness, List<CardSubtype> subtypes) {
        this(power, toughness, subtypes, Set.of());
    }

    public BecomeCreatureEffect(int power, int toughness, CardSubtype subtype) {
        this(power, toughness, List.of(subtype), Set.of());
    }

    public BecomeCreatureEffect(int power, int toughness, CardSubtype subtype, Set<Keyword> keywords) {
        this(power, toughness, List.of(subtype), keywords);
    }

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(null, false, null, true, 1);
    }
}
