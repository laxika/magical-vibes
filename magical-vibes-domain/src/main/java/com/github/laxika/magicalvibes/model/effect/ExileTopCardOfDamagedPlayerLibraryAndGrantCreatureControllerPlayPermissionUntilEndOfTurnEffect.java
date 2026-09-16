package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the top cards of the damaged player's library and lets the controller of the creature
 * that dealt the damage play them until end of turn. The cards may optionally be played without
 * paying their mana costs.
 */
public record ExileTopCardOfDamagedPlayerLibraryAndGrantCreatureControllerPlayPermissionUntilEndOfTurnEffect(
        int count,
        boolean withoutPayingManaCost
) implements CombatDamageTriggerContextEffect {

    public ExileTopCardOfDamagedPlayerLibraryAndGrantCreatureControllerPlayPermissionUntilEndOfTurnEffect() {
        this(1, false);
    }

    public ExileTopCardOfDamagedPlayerLibraryAndGrantCreatureControllerPlayPermissionUntilEndOfTurnEffect {
        if (count <= 0) {
            throw new IllegalArgumentException("count must be positive");
        }
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
