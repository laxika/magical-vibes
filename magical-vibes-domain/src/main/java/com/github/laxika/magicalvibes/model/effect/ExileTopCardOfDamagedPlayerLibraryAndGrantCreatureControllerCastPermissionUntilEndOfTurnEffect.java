package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the top card of the damaged player's library and lets the controller of the creature
 * that dealt the damage cast it until end of turn. Lands do not receive a permission because the
 * effect grants casting permission, not land-play permission.
 */
public record ExileTopCardOfDamagedPlayerLibraryAndGrantCreatureControllerCastPermissionUntilEndOfTurnEffect(
        boolean anyManaType)
        implements CombatDamageTriggerContextEffect {

    public ExileTopCardOfDamagedPlayerLibraryAndGrantCreatureControllerCastPermissionUntilEndOfTurnEffect() {
        this(false);
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
