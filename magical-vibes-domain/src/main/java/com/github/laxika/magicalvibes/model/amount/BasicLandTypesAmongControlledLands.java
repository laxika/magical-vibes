package com.github.laxika.magicalvibes.model.amount;

/**
 * The number of distinct basic land types (Plains, Island, Swamp, Mountain, Forest) among lands
 * in the selected player scope. The default scope is the effect controller. Respects land-type overrides
 * when evaluated outside static computation. Combine with {@link Scaled} for "{2} less for each
 * basic land type" style reductions (Draco).
 */
public record BasicLandTypesAmongControlledLands(CountScope scope) implements DynamicAmount {

    public BasicLandTypesAmongControlledLands() {
        this(CountScope.CONTROLLER);
    }
}
