package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.List;

/**
 * One-shot source effect that permanently changes the source into only a creature with the given
 * base power, base toughness, and creature subtypes.
 */
public record BecomeCreatureEffect(int power, int toughness, List<CardSubtype> subtypes) implements CardEffect {

    public BecomeCreatureEffect {
        subtypes = List.copyOf(subtypes);
    }

    public BecomeCreatureEffect(int power, int toughness, CardSubtype subtype) {
        this(power, toughness, List.of(subtype));
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.NONE;
    }
}
