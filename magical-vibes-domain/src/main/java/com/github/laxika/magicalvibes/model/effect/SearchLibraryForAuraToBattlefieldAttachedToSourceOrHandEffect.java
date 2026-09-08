package com.github.laxika.magicalvibes.model.effect;

/**
 * Searches the controller's library for an Aura that could enchant the source permanent. If the
 * source is still on the battlefield, the Aura enters attached to it; otherwise, the Aura is
 * revealed and put into its controller's hand.
 */
public record SearchLibraryForAuraToBattlefieldAttachedToSourceOrHandEffect() implements CardEffect {
}
