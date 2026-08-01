package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * A battlefield activated ability suspended waiting for an interactive cost choice
 * (e.g. discard a card).
 *
 * @param remainingDiscards how many discard choices are still owed for a multi-card discard cost
 * @param discardCostRequiredName the name every remaining discard must match for a same-name discard
 *                                cost (Sphinx of the Chimes), fixed by the first card chosen;
 *                                {@code null} while no name has been locked in
 */
public record PendingAbilityActivation(UUID sourcePermanentId, int abilityIndex, int xValue,
                                       UUID targetId, Zone targetZone,
                                       String discardCostLabel, int remainingDiscards,
                                       String discardCostRequiredName) {

    public PendingAbilityActivation(UUID sourcePermanentId, int abilityIndex, int xValue,
                                    UUID targetId, Zone targetZone, String discardCostLabel) {
        this(sourcePermanentId, abilityIndex, xValue, targetId, targetZone, discardCostLabel, 1, null);
    }

    public PendingAbilityActivation(UUID sourcePermanentId, int abilityIndex, int xValue,
                                    UUID targetId, Zone targetZone, String discardCostLabel, int remainingDiscards) {
        this(sourcePermanentId, abilityIndex, xValue, targetId, targetZone, discardCostLabel, remainingDiscards, null);
    }
}
