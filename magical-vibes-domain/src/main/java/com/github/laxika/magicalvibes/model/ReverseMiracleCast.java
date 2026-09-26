package com.github.laxika.magicalvibes.model;

import java.util.List;

/** An alternate cost that lets the card be cast from the bottom of its owner's library. */
public record ReverseMiracleCast(List<CastingCost> costs) implements CastingOption {

    public ReverseMiracleCast(String manaCost) {
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
