package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/**
 * Death-trigger descriptor for an ability that destroys the Merfolk tapped to pay for the source
 * permanent's abilities. The trigger collector snapshots the tracked permanent ids at death and
 * converts this descriptor into a regular non-targeting destruction effect.
 */
public record DestroyMerfolkTappedForSourceAbilitiesEffect(List<UUID> tappedPermanentIds)
        implements CardEffect {

    public DestroyMerfolkTappedForSourceAbilitiesEffect() {
        this(List.of());
    }

    public DestroyMerfolkTappedForSourceAbilitiesEffect {
        tappedPermanentIds = List.copyOf(tappedPermanentIds);
    }
}
