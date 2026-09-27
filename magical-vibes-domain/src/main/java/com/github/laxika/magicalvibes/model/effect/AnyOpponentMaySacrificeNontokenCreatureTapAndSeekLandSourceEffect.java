package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Gitrog, Horror of Zhava's combat trigger. */
public record AnyOpponentMaySacrificeNontokenCreatureTapAndSeekLandSourceEffect(
        SeekCardToBattlefieldEffect landSearch,
        List<UUID> remainingOpponentIds,
        UUID abilityControllerId,
        UUID sourcePermanentId,
        boolean anyAccepted
) implements CardEffect {

    public AnyOpponentMaySacrificeNontokenCreatureTapAndSeekLandSourceEffect {
        Objects.requireNonNull(landSearch, "landSearch");
    }

    public AnyOpponentMaySacrificeNontokenCreatureTapAndSeekLandSourceEffect(
            SeekCardToBattlefieldEffect landSearch) {
        this(landSearch, null, null, null, false);
    }
}
