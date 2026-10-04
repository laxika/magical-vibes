package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/**
 * Cast trigger for Brain Gorgers: each player may sacrifice a creature, and the spell is countered
 * after every player has made their choice and the chosen creatures are sacrificed.
 *
 * @param remainingPlayerIds players still to receive the choice
 * @param abilityControllerId controller of the triggered ability
 * @param targetCardId the spell that may be countered
 * @param anyAccepted whether a player has already sacrificed a creature
 */
public record AnyPlayerMaySacrificeCreatureToCounterSpellEffect(
        List<UUID> remainingPlayerIds,
        UUID abilityControllerId,
        UUID targetCardId,
        boolean anyAccepted,
        List<UUID> chosenSacrificePermanentIds
) implements TriggeringSpellReferencingEffect {

    public AnyPlayerMaySacrificeCreatureToCounterSpellEffect(List<UUID> remainingPlayerIds,
            UUID abilityControllerId, UUID targetCardId, boolean anyAccepted) {
        this(remainingPlayerIds, abilityControllerId, targetCardId, anyAccepted, List.of());
    }

    public AnyPlayerMaySacrificeCreatureToCounterSpellEffect {
        chosenSacrificePermanentIds = List.copyOf(chosenSacrificePermanentIds);
    }

    public AnyPlayerMaySacrificeCreatureToCounterSpellEffect() {
        this(null, null, null, false);
    }
}
