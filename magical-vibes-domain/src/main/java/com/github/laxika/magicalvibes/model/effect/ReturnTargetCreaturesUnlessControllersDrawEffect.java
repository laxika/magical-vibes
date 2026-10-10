package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/**
 * For each selected creature, its controller chooses whether the spell's controller draws a card
 * instead of returning that creature to its owner's hand.
 */
public record ReturnTargetCreaturesUnlessControllersDrawEffect(
        List<UUID> remainingTargetIds,
        UUID abilityControllerId,
        List<UUID> returnTargetIds,
        int drawCount
) implements RemovalEffect {

    public ReturnTargetCreaturesUnlessControllersDrawEffect() {
        this(null, null);
    }

    public ReturnTargetCreaturesUnlessControllersDrawEffect(List<UUID> remainingTargetIds, UUID abilityControllerId) {
        this(remainingTargetIds, abilityControllerId, List.of(), 0);
    }

    public ReturnTargetCreaturesUnlessControllersDrawEffect {
        if (remainingTargetIds != null) {
            remainingTargetIds = List.copyOf(remainingTargetIds);
        }
        returnTargetIds = returnTargetIds == null ? List.of() : List.copyOf(returnTargetIds);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.BOUNCE;
    }
}
