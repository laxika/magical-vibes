package com.github.laxika.magicalvibes.model.effect;

/** Searches the controller's library for up to two basic lands, then rolls a d20. */
public record SearchLibraryForUpToTwoBasicLandsThenRollD20Effect(
        D20BasicLandPlacementEffect oneToNine,
        D20BasicLandPlacementEffect tenToNineteen,
        D20BasicLandPlacementEffect twenty) implements CardEffect {

    public SearchLibraryForUpToTwoBasicLandsThenRollD20Effect {
        if (oneToNine == null || tenToNineteen == null || twenty == null) {
            throw new IllegalArgumentException("All d20 basic-land branches are required");
        }
    }
}
