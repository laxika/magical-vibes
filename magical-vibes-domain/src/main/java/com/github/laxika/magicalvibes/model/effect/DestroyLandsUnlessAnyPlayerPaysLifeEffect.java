package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/** Resolves Cleansing's per-land pay-life-or-destroy sequence. */
public record DestroyLandsUnlessAnyPlayerPaysLifeEffect(
        int lifeCost,
        List<UUID> remainingLandIds,
        UUID currentLandId,
        List<UUID> remainingPayerIds
) implements CardEffect {

    public DestroyLandsUnlessAnyPlayerPaysLifeEffect(int lifeCost) {
        this(lifeCost, List.of(), null, List.of());
    }

    public DestroyLandsUnlessAnyPlayerPaysLifeEffect {
        remainingLandIds = remainingLandIds == null ? List.of() : List.copyOf(remainingLandIds);
        remainingPayerIds = remainingPayerIds == null ? List.of() : List.copyOf(remainingPayerIds);
    }
}
