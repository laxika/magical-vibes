package com.github.laxika.magicalvibes.model;

import java.util.List;

/** Marks a double-faced card whose back face can be cast from its owner's hand as an Omen spell. */
public record OmenCast() implements CastingOption {

    @Override
    public Disposition disposition() {
        return Disposition.GRAVEYARD;
    }

    @Override
    public List<CastingCost> costs() {
        return List.of();
    }
}
