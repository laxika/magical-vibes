package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Permanent;

import java.util.List;
import java.util.UUID;

/**
 * Upkeep punisher trigger (Innocent Traveler): each opponent may sacrifice a creature in turn
 * order. All creatures chosen this way are sacrificed simultaneously; if none are chosen, the
 * source transforms. The source snapshot taken when the ability was put on the stack is carried
 * along so the transform is skipped if the source has transformed since then.
 */
public record AnyOpponentMaySacrificeCreatureOrTransformSourceEffect(
        List<UUID> remainingOpponentIds,
        UUID abilityControllerId,
        UUID sourcePermanentId,
        List<UUID> chosenCreatureIds,
        Permanent sourcePermanentSnapshot
) implements CardEffect {

    public AnyOpponentMaySacrificeCreatureOrTransformSourceEffect {
        remainingOpponentIds = remainingOpponentIds == null ? null : List.copyOf(remainingOpponentIds);
        chosenCreatureIds = chosenCreatureIds == null ? List.of() : List.copyOf(chosenCreatureIds);
    }

    public AnyOpponentMaySacrificeCreatureOrTransformSourceEffect() {
        this(null, null, null, List.of(), null);
    }
}
