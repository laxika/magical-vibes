package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Prompts for a player or planeswalker and makes the chosen permanent attack that object.
 */
public record MakeChosenPermanentAttackingEffect(UUID permanentId,
                                                 boolean onlyTargetsAttackedByTriggeringPlayer)
        implements CardEffect {

    public MakeChosenPermanentAttackingEffect(UUID permanentId) {
        this(permanentId, false);
    }

    /** Tahngarth: the target must be one of the triggering opponent's current attack targets. */
    public static MakeChosenPermanentAttackingEffect attackingTargetOfTriggeringPlayer() {
        return new MakeChosenPermanentAttackingEffect(null, true);
    }

    public static MakeChosenPermanentAttackingEffect attackingTargetOfTriggeringPlayer(UUID permanentId) {
        return new MakeChosenPermanentAttackingEffect(permanentId, true);
    }
}
