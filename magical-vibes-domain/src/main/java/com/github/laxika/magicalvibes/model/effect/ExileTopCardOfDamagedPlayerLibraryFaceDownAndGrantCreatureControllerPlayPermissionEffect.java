package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the top card of the combat-damaged player's library face down and lets the controller of
 * the creature that dealt the damage play it for as long as it remains exiled. When
 * {@code anyManaTypeSpendable} is set, mana of any type can be spent to cast that card. When
 * {@code grantToSourceController} is set, the permission instead goes to the controller of the
 * ability's source ("you may play it", as for Dream-Thief's Bandana's trigger).
 */
public record ExileTopCardOfDamagedPlayerLibraryFaceDownAndGrantCreatureControllerPlayPermissionEffect(
        boolean anyManaTypeSpendable,
        boolean grantToSourceController
) implements CombatDamageTriggerContextEffect {

    public ExileTopCardOfDamagedPlayerLibraryFaceDownAndGrantCreatureControllerPlayPermissionEffect(
            boolean anyManaTypeSpendable) {
        this(anyManaTypeSpendable, false);
    }

    public ExileTopCardOfDamagedPlayerLibraryFaceDownAndGrantCreatureControllerPlayPermissionEffect() {
        this(true, false);
    }

    /** The ability's controller (not the creature's) may play the card, with any type of mana. */
    public static ExileTopCardOfDamagedPlayerLibraryFaceDownAndGrantCreatureControllerPlayPermissionEffect forSourceController() {
        return new ExileTopCardOfDamagedPlayerLibraryFaceDownAndGrantCreatureControllerPlayPermissionEffect(true, true);
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
