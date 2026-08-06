package com.github.laxika.magicalvibes.model.effect;

/**
 * Signal the Clans. The controller searches their library for three creature cards and reveals
 * them; if the three revealed cards all have different names, one of them chosen at random goes
 * into the controller's hand. Every other revealed card is shuffled back into the library.
 *
 * <p>The picks run as a repeated
 * {@link com.github.laxika.magicalvibes.model.LibrarySearchDestination#SIGNAL_THE_CLANS_POOL}
 * search, which holds the revealed cards outside every zone until the search ends.
 */
public record SignalTheClansEffect() implements CardEffect {
}
