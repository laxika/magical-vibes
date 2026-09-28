package com.github.laxika.magicalvibes.model;

import java.util.List;

/**
 * Flashforward: cast this card from exile for its flashforward cost, then put it on the bottom
 * of its owner's library.
 */
public record FlashforwardCast(List<CastingCost> costs) implements CastingOption {

    public FlashforwardCast(String manaCost) {
        this(List.of(new ManaCastingCost(manaCost)));
    }

    public FlashforwardCast {
        costs = costs == null ? List.of() : List.copyOf(costs);
    }

    @Override
    public Disposition disposition() {
        return Disposition.BOTTOM_OF_LIBRARY;
    }
}
