package com.github.laxika.magicalvibes.model.effect;

/**
 * Master of Predicaments' combat-damage trigger: choose a card in hand, then the damaged player
 * guesses whether its mana value is greater than four.
 */
public record MasterOfPredicamentsEffect() implements CombatDamageTriggerContextEffect {

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
