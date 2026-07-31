package com.github.laxika.magicalvibes.model.effect;

/**
 * Deals damage to a target player or planeswalker and damage to up to {@code maxCreatureTargets}
 * target creatures that player — or that planeswalker's controller — controls.
 *
 * <p>The chosen targets are stored on the stack entry as targetIds[0] = the player or planeswalker
 * and, if chosen, targetIds[1..N] = creature permanent IDs. A creature that is no longer controlled
 * by the affected player on resolution is skipped.
 *
 * <p>{@code opponentOnly} restricts the first target to an opponent and forbids planeswalkers
 * (Ravager of the Fells); {@code creaturesCantBlock} adds the "that creature can't block this turn"
 * rider (Chandra, Pyromaster).
 *
 * @param playerDamage       damage dealt to the player or planeswalker
 * @param creatureDamage     damage dealt to each chosen creature
 * @param maxCreatureTargets how many creatures may be chosen
 * @param opponentOnly       whether the first target must be an opponent (no planeswalkers)
 * @param creaturesCantBlock whether the damaged creatures also can't block this turn
 */
public record DealDamageToTargetAndUpToCreaturesThatPlayerControlsEffect(
        int playerDamage,
        int creatureDamage,
        int maxCreatureTargets,
        boolean opponentOnly,
        boolean creaturesCantBlock
) implements CardEffect {

    public DealDamageToTargetAndUpToCreaturesThatPlayerControlsEffect(int playerDamage,
                                                                     int creatureDamage,
                                                                     int maxCreatureTargets) {
        this(playerDamage, creatureDamage, maxCreatureTargets, true, false);
    }

    public DealDamageToTargetAndUpToCreaturesThatPlayerControlsEffect {
        if (maxCreatureTargets < 0) {
            throw new IllegalArgumentException("maxCreatureTargets must be non-negative");
        }
    }
}
