package com.github.laxika.magicalvibes.model;

import java.util.List;

/**
 * Flashback: cast this spell from the graveyard for its flashback cost,
 * then exile it instead of putting it anywhere else (CR 702.33a).
 */
public record FlashbackCast(List<CastingCost> costs) implements CastingOption {

    /**
     * Convenience constructor for pure mana flashback (the common case).
     */
    public FlashbackCast(String manaCost) {
        this(List.of(new ManaCastingCost(manaCost)));
    }

    @Override
    public Disposition disposition() {
        return Disposition.EXILE;
    }
}
