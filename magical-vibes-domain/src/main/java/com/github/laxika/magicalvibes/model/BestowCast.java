package com.github.laxika.magicalvibes.model;

import java.util.List;

public record BestowCast(List<CastingCost> costs) implements CastingOption {

    public BestowCast(String manaCost) {
        this(List.of(new ManaCastingCost(manaCost)));
    }

    @Override
    public Disposition disposition() {
        return Disposition.GRAVEYARD;
    }
}
