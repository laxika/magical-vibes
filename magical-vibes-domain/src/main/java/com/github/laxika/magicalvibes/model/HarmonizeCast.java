package com.github.laxika.magicalvibes.model;

import java.util.List;

/**
 * Cast this spell from the graveyard for its harmonize mana cost. The cast may
 * additionally tap one untapped creature controlled by the caster to reduce
 * the generic part of that cost by the creature's power.
 */
public record HarmonizeCast(List<CastingCost> costs) implements CastingOption {

    public HarmonizeCast(String manaCost) {
        this(List.of(new ManaCastingCost(manaCost)));
    }

    @Override
    public Disposition disposition() {
        return Disposition.EXILE;
    }
}
