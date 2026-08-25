package com.github.laxika.magicalvibes.model;

import java.util.List;

/** Marks a card whose back face can be cast as an Adventure spell from its owner's hand. */
public record AdventureCast() implements CastingOption {

    @Override
    public Disposition disposition() {
        return Disposition.GRAVEYARD;
    }

    @Override
    public List<CastingCost> costs() {
        return List.of();
    }
}
