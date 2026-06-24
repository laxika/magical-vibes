package com.github.laxika.magicalvibes.model.effect;

/**
 * Deals damage to a target opponent and damage to up to {@code maxCreatureTargets}
 * target creatures that player controls.
 *
 * <p>The chosen targets are stored on the stack entry as targetIds[0] = opponent player ID and,
 * if chosen, targetIds[1..N] = creature permanent IDs.
 */
public record DealDamageToTargetOpponentAndUpToCreaturesThatPlayerControlsEffect(
        int opponentDamage,
        int creatureDamage,
        int maxCreatureTargets
) implements CardEffect {

    public DealDamageToTargetOpponentAndUpToCreaturesThatPlayerControlsEffect(int opponentDamage,
                                                                             int creatureDamage) {
        this(opponentDamage, creatureDamage, 1);
    }

    public DealDamageToTargetOpponentAndUpToCreaturesThatPlayerControlsEffect {
        if (maxCreatureTargets < 0) {
            throw new IllegalArgumentException("maxCreatureTargets must be non-negative");
        }
    }
}
