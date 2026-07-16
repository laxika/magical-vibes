package com.github.laxika.magicalvibes.model.amount;

/**
 * The number of distinct basic land types (Plains, Island, Swamp, Mountain, Forest) among lands
 * the controller controls — the Domain count (CR 702.42). Respects CR 305.7 land-type overrides
 * when evaluated outside static computation. Combine with {@link Scaled} for "{2} less for each
 * basic land type" style reductions (Draco).
 */
public record BasicLandTypesAmongControlledLands() implements DynamicAmount {
}
