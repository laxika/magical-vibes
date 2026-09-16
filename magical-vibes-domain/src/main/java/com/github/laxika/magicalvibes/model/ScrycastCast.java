package com.github.laxika.magicalvibes.model;

import java.util.List;

/** Scrycast: cast this card for its alternate cost while scrying. */
public record ScrycastCast(List<CastingCost> costs) implements CastingOption {

    public ScrycastCast(String manaCost) {
        this(List.of(new ManaCastingCost(manaCost)));
    }

    @Override
    public Disposition disposition() {
        return Disposition.GRAVEYARD;
    }

    public String manaCostString() {
        return getCost(ManaCastingCost.class).map(ManaCastingCost::manaCost).orElse(null);
    }
}
