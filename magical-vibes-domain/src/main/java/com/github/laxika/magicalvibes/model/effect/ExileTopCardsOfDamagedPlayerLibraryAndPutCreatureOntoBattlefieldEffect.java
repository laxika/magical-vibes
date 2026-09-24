package com.github.laxika.magicalvibes.model.effect;

/** Exiles the top cards of the combat-damaged player's library, then puts one exiled creature onto the battlefield. */
public record ExileTopCardsOfDamagedPlayerLibraryAndPutCreatureOntoBattlefieldEffect(int count)
        implements CombatDamageTriggerContextEffect {

    public ExileTopCardsOfDamagedPlayerLibraryAndPutCreatureOntoBattlefieldEffect {
        if (count < 0) {
            throw new IllegalArgumentException("count cannot be negative");
        }
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
