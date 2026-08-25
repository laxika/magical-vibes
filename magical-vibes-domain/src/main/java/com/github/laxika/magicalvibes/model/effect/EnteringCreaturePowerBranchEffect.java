package com.github.laxika.magicalvibes.model.effect;

/**
 * Ally-creature-enters marker that chooses one of two effects from the entering creature's
 * effective power when the triggered ability resolves.
 */
public record EnteringCreaturePowerBranchEffect(
        int minPower,
        CardEffect powerAtLeast,
        CardEffect belowPower
) implements CardEffect {
}
