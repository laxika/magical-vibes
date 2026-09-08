package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.condition.Condition;

import java.util.List;

/**
 * Flashback: cast this spell from the graveyard for its flashback cost,
 * then exile it instead of putting it anywhere else (CR 702.33a).
 */
public record FlashbackCast(List<CastingCost> costs, Condition availabilityCondition) implements CastingOption {

    public FlashbackCast(List<CastingCost> costs) {
        this(costs, null);
    }

    /**
     * Convenience constructor for pure mana flashback (the common case).
     */
    public FlashbackCast(String manaCost) {
        this(List.of(new ManaCastingCost(manaCost)), null);
    }

    public FlashbackCast(String manaCost, Condition availabilityCondition) {
        this(List.of(new ManaCastingCost(manaCost)), availabilityCondition);
    }

    @Override
    public Disposition disposition() {
        return Disposition.EXILE;
    }
}
