package com.github.laxika.magicalvibes.model.effect;

/**
 * Where a spell countered by a {@link CounterSpellEffect} ends up. Normally a countered spell goes to
 * its owner's graveyard (CR 701.5g); some counterspells send the card elsewhere instead
 * (Dissipate → exile, Memory Lapse → top of the owner's library). Copies of spells and countered
 * abilities cease to exist regardless of this value (they never had a card to place).
 */
public enum CounteredSpellDestination {
    /** The countered card goes to its owner's graveyard (default). */
    GRAVEYARD,
    /** The countered card is exiled instead of going to the graveyard (Dissipate, Faerie Trickery). */
    EXILE,
    /** The countered card is put on top of its owner's library instead of the graveyard (Memory Lapse). */
    LIBRARY_TOP
}
