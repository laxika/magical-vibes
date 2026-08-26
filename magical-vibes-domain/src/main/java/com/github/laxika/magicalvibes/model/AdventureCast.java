package com.github.laxika.magicalvibes.model;

import java.util.List;

/** Marks an adventurer card whose alternative face can be cast from its owner's hand. */
public record AdventureCast(List<CastingCost> costs) implements CastingOption {

    public AdventureCast(String manaCost) {
        this(List.of(new ManaCastingCost(manaCost)));
    }

    @Override
    public Disposition disposition() {
        return Disposition.GRAVEYARD;
    }
}
