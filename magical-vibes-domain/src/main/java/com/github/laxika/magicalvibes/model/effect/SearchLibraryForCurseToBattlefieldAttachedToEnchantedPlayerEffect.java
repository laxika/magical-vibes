package com.github.laxika.magicalvibes.model.effect;

/**
 * Searches the controller's library for a Curse card that doesn't have the same name as a
 * Curse already attached to the source aura's enchanted player, then puts it onto the
 * battlefield attached to that same enchanted player and shuffles. Used by Curse of Misfortunes.
 *
 * <p>The enchanted player is derived at resolution time from the source permanent's
 * {@code attachedTo} (the aura controlling this effect), so this effect carries no parameters.
 */
public record SearchLibraryForCurseToBattlefieldAttachedToEnchantedPlayerEffect() implements CardEffect {
}
