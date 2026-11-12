package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Prompts for a player or planeswalker and makes the chosen permanent attack that object.
 *
 * @param permanentId the permanent an engine handler makes attacking in the resolution that put it onto the
 *                    battlefield; {@code null} in a card's effect
 * @param onlyTargetsAttackedByTriggeringPlayer whether it can only attack what the triggering player is attacking
 * @param picked whether the permanent is the one an earlier step of the resolution picked (the first of the
 *                        resolving entry's picks: "put it onto the battlefield tapped and attacking") instead of the
 *                        ability's source
 */
public record MakeChosenPermanentAttackingEffect(UUID permanentId,
                                                 boolean onlyTargetsAttackedByTriggeringPlayer,
                                                 boolean picked)
        implements CardEffect {

    public MakeChosenPermanentAttackingEffect(UUID permanentId) {
        this(permanentId, false, false);
    }

    /** "It enters attacking": the permanent an earlier step of the resolution picked. */
    public static MakeChosenPermanentAttackingEffect pickedPermanent() {
        return new MakeChosenPermanentAttackingEffect(null, false, true);
    }

    /** Tahngarth: the target must be one of the triggering opponent's current attack targets. */
    public static MakeChosenPermanentAttackingEffect attackingTargetOfTriggeringPlayer() {
        return new MakeChosenPermanentAttackingEffect(null, true, false);
    }
}
