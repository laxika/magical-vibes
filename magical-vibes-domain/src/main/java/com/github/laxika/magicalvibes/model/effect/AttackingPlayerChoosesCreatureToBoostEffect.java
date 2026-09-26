package com.github.laxika.magicalvibes.model.effect;

/** The attacking player chooses one of their attacking creatures to get a temporary boost. */
public record AttackingPlayerChoosesCreatureToBoostEffect(int powerBoost, int toughnessBoost)
        implements CardEffect {
}
