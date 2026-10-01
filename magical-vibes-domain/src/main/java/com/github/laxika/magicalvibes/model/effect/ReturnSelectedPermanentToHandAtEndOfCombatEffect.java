package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/** Schedules a permanent selected by a library-to-battlefield effect to return to its owner's hand. */
public record ReturnSelectedPermanentToHandAtEndOfCombatEffect(UUID permanentId)
        implements CardEffect {

    public static ReturnSelectedPermanentToHandAtEndOfCombatEffect forSelectedPermanent(
            List<UUID> selectedPermanentIds) {
        return new ReturnSelectedPermanentToHandAtEndOfCombatEffect(selectedPermanentIds.getFirst());
    }
}
