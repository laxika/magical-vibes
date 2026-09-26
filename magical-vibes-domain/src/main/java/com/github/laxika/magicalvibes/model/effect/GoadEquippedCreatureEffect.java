package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsHostOfSourceAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Static combat requirement that goads the creature hosting this Equipment or Aura. */
public record GoadEquippedCreatureEffect() implements CombatAttackRequirementEffect {

    private static final PermanentPredicate EQUIPPED_CREATURE = new PermanentIsHostOfSourceAuraPredicate();

    @Override
    public PermanentPredicate affectedPredicate() {
        return EQUIPPED_CREATURE;
    }

    @Override
    public boolean requiresAttackAtOtherPlayerIfAble() {
        return true;
    }
}
