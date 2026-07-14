package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

public record ReturnPermanentsOnCombatDamageToPlayerEffect(PermanentPredicate filter)
        implements CombatDamageTriggerContextEffect {

    public ReturnPermanentsOnCombatDamageToPlayerEffect() {
        this(null);
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER_WITH_DAMAGE_AMOUNT;
    }
}
