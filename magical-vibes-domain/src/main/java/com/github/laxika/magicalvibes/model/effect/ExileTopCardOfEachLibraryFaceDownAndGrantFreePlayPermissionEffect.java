package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the top card of each player's library face down and lets the effect controller play
 * those cards without paying their mana costs for as long as they remain exiled.
 */
public record ExileTopCardOfEachLibraryFaceDownAndGrantFreePlayPermissionEffect()
        implements CardEffect {
}
