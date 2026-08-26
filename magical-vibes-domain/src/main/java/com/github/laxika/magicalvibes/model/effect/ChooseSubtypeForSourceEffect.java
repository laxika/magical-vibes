package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.List;

/**
 * On resolution, the controller secretly chooses a creature type and stores it on the source
 * permanent.
 */
public record ChooseSubtypeForSourceEffect(List<CardSubtype> allowedSubtypes) implements CardEffect {

    public ChooseSubtypeForSourceEffect() {
        this(List.of());
    }

    public ChooseSubtypeForSourceEffect {
        allowedSubtypes = allowedSubtypes == null ? List.of() : List.copyOf(allowedSubtypes);
    }
}
