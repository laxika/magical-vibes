package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

import java.util.List;
import java.util.UUID;

/** Manifests dread a dynamic number of times, then puts that many +1/+1 counters on each manifested permanent. */
public record ManifestDreadXTimesThenPutCountersEffect(
        DynamicAmount amount,
        Integer remainingManifestations,
        int counterCount,
        List<UUID> manifestedPermanentIds,
        boolean awaitingManifestationChoice
) implements CardEffect {

    public ManifestDreadXTimesThenPutCountersEffect(DynamicAmount amount) {
        this(amount, null, 0, List.of(), false);
    }

    public ManifestDreadXTimesThenPutCountersEffect {
        manifestedPermanentIds = List.copyOf(manifestedPermanentIds);
    }

    public boolean initialized() {
        return remainingManifestations != null;
    }
}
