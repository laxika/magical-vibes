package com.github.laxika.magicalvibes.model.effect;

/**
 * Doubles the life total of the enchanted creature's controller when used by an Aura's combat
 * damage trigger. The combat trigger context carries that player on the stack entry.
 */
public record DoubleEnchantedCreatureControllerLifeEffect() implements CombatDamageTriggerContextEffect {

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.ENCHANTED_CREATURE_CONTROLLER;
    }
}
