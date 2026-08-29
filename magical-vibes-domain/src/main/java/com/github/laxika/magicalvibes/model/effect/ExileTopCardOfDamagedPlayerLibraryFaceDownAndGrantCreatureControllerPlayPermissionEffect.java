package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the top card of the combat-damaged player's library face down and lets the controller of
 * the creature that dealt the damage play it for as long as it remains exiled.
 */
public record ExileTopCardOfDamagedPlayerLibraryFaceDownAndGrantCreatureControllerPlayPermissionEffect()
        implements CardEffect {
}
