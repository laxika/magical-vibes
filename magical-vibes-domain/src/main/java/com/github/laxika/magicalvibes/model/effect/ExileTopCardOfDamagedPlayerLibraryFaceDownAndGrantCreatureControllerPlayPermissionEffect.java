package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the top card of the combat-damaged player's library face down and lets the controller of
 * the creature that dealt the damage play it for as long as it remains exiled. When
 * {@code anyManaTypeSpendable} is set, mana of any type can be spent to cast that card.
 */
public record ExileTopCardOfDamagedPlayerLibraryFaceDownAndGrantCreatureControllerPlayPermissionEffect(
        boolean anyManaTypeSpendable
) implements CombatDamageTriggerContextEffect {

    public ExileTopCardOfDamagedPlayerLibraryFaceDownAndGrantCreatureControllerPlayPermissionEffect() {
        this(true);
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
