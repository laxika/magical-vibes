package com.github.laxika.magicalvibes.model;

import java.util.List;

/** Marks a double-faced card whose back face can be cast from its owner's hand as an Adventure. */
public record AdventureCast(List<CastingCost> costs) implements CastingOption {

    public AdventureCast() {
        this(List.of());
    }

    public AdventureCast(String manaCost) {
        this(List.of(new ManaCastingCost(manaCost)));
    }

    @Override
    public Disposition disposition() {
        return Disposition.EXILE;
    }
}
