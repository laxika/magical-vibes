package com.github.laxika.magicalvibes.model;

import java.util.List;

/**
 * Foretell: exile this card face down from hand by paying the special action cost, then cast it
 * from exile on a later turn for its foretell cost.
 */
public record ForetellCast(List<CastingCost> costs) implements CastingOption {

    public ForetellCast(String manaCost) {
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
