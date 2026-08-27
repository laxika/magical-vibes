package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.List;

/**
 * As the source enters, its controller secretly chooses a creature type and stores it on the
 * permanent.
 */
public record ChooseSubtypeForSourceEffect(List<CardSubtype> allowedSubtypes)
        implements SubtypeChoiceOnEnterEffect {

    public ChooseSubtypeForSourceEffect() {
        this(List.of());
    }

    public ChooseSubtypeForSourceEffect {
        allowedSubtypes = allowedSubtypes == null ? List.of() : List.copyOf(allowedSubtypes);
    }
}
