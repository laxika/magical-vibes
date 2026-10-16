package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Goads every matching creature until the effect controller's next turn. The matching set is
 * evaluated continuously, so creatures that enter later are affected as well.
 */
public record GoadCreaturesUntilNextTurnEffect(PermanentPredicate affectedPredicate, boolean makesGoaded)
        implements CombatAttackRequirementEffect, GoadStatusEffect {

    public GoadCreaturesUntilNextTurnEffect(PermanentPredicate affectedPredicate) {
        this(affectedPredicate, true);
    }

    /**
     * The same attack requirement without the goaded designation, for text such as Kardur,
     * Doomscourge's "attack each combat if able and attack a player other than you if able".
     */
    public static GoadCreaturesUntilNextTurnEffect requirementOnly(PermanentPredicate affectedPredicate) {
        return new GoadCreaturesUntilNextTurnEffect(affectedPredicate, false);
    }

    @Override
    public boolean requiresAttackAtOtherPlayerIfAble() {
        return true;
    }
}
