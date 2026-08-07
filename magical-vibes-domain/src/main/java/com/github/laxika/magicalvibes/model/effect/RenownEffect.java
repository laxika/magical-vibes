package com.github.laxika.magicalvibes.model.effect;

/**
 * Renown N (CR 702.112a): "When this creature deals combat damage to a player, if it isn't
 * renowned, put N +1/+1 counters on it and it becomes renowned." Put on
 * {@code EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER}; the "isn't renowned" check is part of the
 * resolution, so a second instance resolving later simply does nothing (CR 702.112c).
 */
public record RenownEffect(int amount) implements CombatDamageTriggerContextEffect {

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.SOURCE_SELF;
    }
}
