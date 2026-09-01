package com.github.laxika.magicalvibes.model.effect;

/**
 * Waterbend cost with either a fixed generic mana amount or an amount that scales with X. Each
 * untapped artifact or creature tapped while paying this cost pays for one generic mana. Optional
 * costs may be declined.
 */
public record WaterbendCost(int amount, boolean scalesWithX, boolean optional) implements CostEffect {

    public WaterbendCost(int amount) {
        this(amount, false, false);
    }

    public WaterbendCost(int amount, boolean scalesWithX) {
        this(amount, scalesWithX, false);
    }

    public static WaterbendCost optional(int amount) {
        return new WaterbendCost(amount, false, true);
    }

    public static WaterbendCost x() {
        return new WaterbendCost(0, true, false);
    }

    public WaterbendCost {
        if ((!scalesWithX && amount <= 0) || (scalesWithX && amount != 0)) {
            throw new IllegalArgumentException("Waterbend cost must be positive");
        }
    }

    public int effectiveAmount(int announcedXValue) {
        return scalesWithX ? Math.max(0, announcedXValue) : amount;
    }
}
