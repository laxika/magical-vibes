package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/**
 * Cast trigger for Brain Gorgers: each player may sacrifice a creature, and the spell is countered
 * as soon as any player does. Remaining players still receive their choices.
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
        boolean anyAccepted
) implements TriggeringSpellReferencingEffect {

    public AnyPlayerMaySacrificeCreatureToCounterSpellEffect() {
        this(null, null, null, false);
    }
}
