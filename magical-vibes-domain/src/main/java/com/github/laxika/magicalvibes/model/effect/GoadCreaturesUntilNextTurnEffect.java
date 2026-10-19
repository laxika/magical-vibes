package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Goads every matching creature until the effect controller's next turn. The matching set is
 * evaluated continuously, so creatures that enter later are affected as well.
 */
public record GoadCreaturesUntilNextTurnEffect(PermanentPredicate affectedPredicate, boolean makesGoaded,
                                               boolean requiresOtherPlayer)
        implements CombatAttackRequirementEffect, GoadStatusEffect {

    public GoadCreaturesUntilNextTurnEffect(PermanentPredicate affectedPredicate) {
        this(affectedPredicate, true, true);
    }

    public GoadCreaturesUntilNextTurnEffect(PermanentPredicate affectedPredicate, boolean makesGoaded) {
        this(affectedPredicate, makesGoaded, true);
    }

    /**
     * The same attack requirement without the goaded designation, for text such as Kardur,
     * Doomscourge's "attack each combat if able and attack a player other than you if able".
     */
    public static GoadCreaturesUntilNextTurnEffect requirementOnly(PermanentPredicate affectedPredicate) {
        return new GoadCreaturesUntilNextTurnEffect(affectedPredicate, false, true);
    }

    /**
     * Only "attack each combat if able" until the controller's next turn: no goaded designation and
     * no restriction on which player is attacked (The Akroan War chapter II).
     */
    public static GoadCreaturesUntilNextTurnEffect mustAttackOnly(PermanentPredicate affectedPredicate) {
        return new GoadCreaturesUntilNextTurnEffect(affectedPredicate, false, false);
    }

    @Override
    public boolean requiresAttackAtOtherPlayerIfAble() {
        return requiresOtherPlayer;
    }
}
