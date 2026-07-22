package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * A battlefield activated ability suspended waiting for an interactive cost choice
 * (e.g. discard a card).
 *
 * @param remainingDiscards how many discard choices are still owed for a multi-card discard cost
 */
public record PendingAbilityActivation(UUID sourcePermanentId, int abilityIndex, int xValue,
                                       UUID targetId, Zone targetZone,
                                       String discardCostLabel, int remainingDiscards) {

    public PendingAbilityActivation(UUID sourcePermanentId, int abilityIndex, int xValue,
                                    UUID targetId, Zone targetZone, String discardCostLabel) {
        this(sourcePermanentId, abilityIndex, xValue, targetId, targetZone, discardCostLabel, 1);
    }
}
