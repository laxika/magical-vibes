package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Goads every matching creature until the effect controller's next turn. The matching set is
 * evaluated continuously, so creatures that enter later are affected as well.
 */
public record GoadCreaturesUntilNextTurnEffect(PermanentPredicate affectedPredicate)
        implements CombatAttackRequirementEffect {

    @Override
    public boolean requiresAttackAtOtherPlayerIfAble() {
        return true;
    }
}
