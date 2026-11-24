package com.github.laxika.magicalvibes.model.effect;

/**
 * Which lands land-derived mana effects read when determining which mana types are available.
 */
public enum ManaColorLandScope {
    /** Lands the ability's controller controls (e.g. Star Compass — "a basic land you control"). */
    CONTROLLER,
    /** Lands an opponent controls (e.g. Fellwar Stone — "a land an opponent controls"). */
    OPPONENTS,
    /**
     * The land sacrificed to pay the ability's cost, as it last existed (CR 608.2h): "any type the sacrificed land
     * could produce" (Squandered Resources). The cost records it on the source as it is paid.
     */
    SACRIFICED_FOR_COST,
    /**
     * The land untapped to pay the ability's cost: "any type that land could produce" (Benthic Explorers). The cost
     * records it on the source as it is paid.
     */
    UNTAPPED_FOR_COST;

    /** Whether the scope names the one land a cost of the ability paid with, rather than players' lands. */
    public boolean paidInCost() {
        return this == SACRIFICED_FOR_COST || this == UNTAPPED_FOR_COST;
    }
}
