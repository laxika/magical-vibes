package com.github.laxika.magicalvibes.model.effect;

/**
 * Ally-creature-enters marker that compares the entering creature's power with the source
 * permanent's power when the triggered ability resolves.
 */
public record EnteringCreatureSourcePowerBranchEffect(
        CardEffect sourcePowerAtLeast,
        CardEffect belowSourcePower
) implements CardEffect {
}
