package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "Exile all [permanents]" with a rider of its own: the cards tracked with the source (Day of the Dragons, Laezel's
 * Acrobatics) and returned one at each upkeep (Dimensional Breach), perpetual changes and the owner's permission to play
 * them (Divine Purge), or the controller's permission (Heartless Conscription). The matching count is the entry's event
 * value. A plain "exile all" is {@link ExileTargetPermanentEffect#all}.
 */
public record ExileAllPermanentsEffect(PermanentPredicate filter, boolean trackWithSource,
                                       boolean returnOneAtEachUpkeep, boolean ownerMayPlayWhileExiled,
                                       int perpetualCastCostIncrease, boolean perpetualEnterTapped,
                                       boolean controllerMayPlayWhileExiled,
                                       boolean controllerMaySpendAnyManaType)
        implements CardEffect {

    public ExileAllPermanentsEffect(PermanentPredicate filter, boolean trackWithSource) {
        this(filter, trackWithSource, false, false, 0, false, false, false);
    }

    public ExileAllPermanentsEffect(PermanentPredicate filter, boolean trackWithSource,
                                    boolean returnOneAtEachUpkeep) {
        this(filter, trackWithSource, returnOneAtEachUpkeep, false, 0, false, false, false);
    }

    public ExileAllPermanentsEffect(PermanentPredicate filter, boolean trackWithSource,
                                    boolean returnOneAtEachUpkeep, boolean ownerMayPlayWhileExiled,
                                    int perpetualCastCostIncrease, boolean perpetualEnterTapped) {
        this(filter, trackWithSource, returnOneAtEachUpkeep, ownerMayPlayWhileExiled,
                perpetualCastCostIncrease, perpetualEnterTapped, false, false);
    }

    /** Exiles matching permanents and lets the resolving spell's controller play them while exiled. */
    public static ExileAllPermanentsEffect withControllerPlayPermission(
            PermanentPredicate filter, boolean anyManaType) {
        return new ExileAllPermanentsEffect(
                filter, false, false, false, 0, false, true, anyManaType);
    }
}
