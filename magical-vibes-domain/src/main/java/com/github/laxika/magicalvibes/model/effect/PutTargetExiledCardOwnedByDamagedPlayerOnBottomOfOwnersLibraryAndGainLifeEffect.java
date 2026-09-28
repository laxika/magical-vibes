package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** Moves a targeted face-up card owned by the damaged player from exile to its owner's library bottom, then gains life. */
public record PutTargetExiledCardOwnedByDamagedPlayerOnBottomOfOwnersLibraryAndGainLifeEffect(int lifeGain)
        implements LifeGainEffect, CombatDamageTriggerContextEffect {

    @Override
    public DynamicAmount lifeGainAmount() {
        return new Fixed(lifeGain);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.exileCard());
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
