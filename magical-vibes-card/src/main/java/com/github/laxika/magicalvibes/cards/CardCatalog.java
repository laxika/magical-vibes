package com.github.laxika.magicalvibes.cards;

import java.util.List;

/**
 * Read access to what is known about each {@link CardSet}: which printings are implemented, and
 * the set metadata the oracle data supplies.
 *
 * <p>This interface exists so that this module can be read from without depending on the module
 * that populates it. The implementation lives in {@code magical-vibes-card-data} because it needs
 * an oracle loader to fill itself, and the module graph runs card-data → card. Everything here
 * used to be static state on {@link CardSet} itself; it moved off the enum so that the data has a
 * lifecycle (a test can hold its own catalog instead of scrubbing JVM-wide maps between cases).
 *
 * <p>Types in this module that cannot be dependency-injected — {@link CardSet} and
 * {@link PrebuiltDeck} are enums, and their constants are built in static initializers — take a
 * catalog as a method parameter instead.
 */
public interface CardCatalog {

    /** Implemented printings of the set, ordered by collector number. Never null; may be empty. */
    List<CardPrinting> getPrintings(CardSet set);

    /**
     * @throws IllegalArgumentException if the set has no implemented printing with that number
     */
    CardPrinting findByCollectorNumber(CardSet set, String collectorNumber);

    /** The set's full name from the oracle data, falling back to its code when not loaded. */
    String getName(CardSet set);

    /**
     * How many cards the set actually contains per the loaded oracle data — the denominator of
     * {@link #getImplementedFraction}. Returns 0 when the oracle data has not been loaded.
     */
    int getSetCardTotal(CardSet set);

    /**
     * Fraction (0..1) of the set's real card pool that is implemented: implemented printings over
     * the set's total card count. Returns 0 when the total is unknown.
     */
    double getImplementedFraction(CardSet set);
}
