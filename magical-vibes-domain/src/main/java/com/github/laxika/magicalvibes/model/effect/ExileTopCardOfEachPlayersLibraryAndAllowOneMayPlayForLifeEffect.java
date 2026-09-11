package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the top card of each player's library and grants the controller one temporary permission
 * to play one of those cards, using life equal to a spell's mana value instead of its mana cost.
 */
public record ExileTopCardOfEachPlayersLibraryAndAllowOneMayPlayForLifeEffect()
        implements CombatDamageTriggerContextEffect {

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.SOURCE_SELF;
    }
}
