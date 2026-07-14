package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

public record SacrificeCreatureCost(boolean trackSacrificedManaValue, boolean trackSacrificedPower, boolean trackSacrificedToughness, boolean excludeSelf, ManaColor trackSacrificedColorSymbols) implements CostEffect {

    private static final PermanentPredicate CREATURE_FILTER = new PermanentIsCreaturePredicate();

    @Override
    public PermanentPredicate consumedPermanentFilter() {
        return CREATURE_FILTER;
    }

    @Override
    public boolean sacrificesChosenCreature() {
        return true;
    }

    public SacrificeCreatureCost() {
        this(false, false, false, false, null);
    }

    public SacrificeCreatureCost(boolean trackSacrificedManaValue) {
        this(trackSacrificedManaValue, false, false, false, null);
    }

    public SacrificeCreatureCost(boolean trackSacrificedManaValue, boolean trackSacrificedPower) {
        this(trackSacrificedManaValue, trackSacrificedPower, false, false, null);
    }

    public SacrificeCreatureCost(boolean trackSacrificedManaValue, boolean trackSacrificedPower, boolean trackSacrificedToughness) {
        this(trackSacrificedManaValue, trackSacrificedPower, trackSacrificedToughness, false, null);
    }

    public SacrificeCreatureCost(boolean trackSacrificedManaValue, boolean trackSacrificedPower, boolean trackSacrificedToughness, boolean excludeSelf) {
        this(trackSacrificedManaValue, trackSacrificedPower, trackSacrificedToughness, excludeSelf, null);
    }

    /** Snapshots the number of {@code color} mana symbols in the sacrificed creature's mana cost into the entry's xValue (Fiery Bombardment). */
    public SacrificeCreatureCost(ManaColor trackSacrificedColorSymbols) {
        this(false, false, false, false, trackSacrificedColorSymbols);
    }
}
