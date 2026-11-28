package com.github.laxika.magicalvibes.cards;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.OracleData;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

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

    /**
     * Creates the card of a printing named by set code and collector number, or returns null when
     * the set is unknown or has no implemented printing with that number. Spellbooks and conjure
     * effects name their cards this way, and may list printings that are not implemented yet.
     */
    default Card createCardIfImplemented(String setCode, String collectorNumber) {
        CardSet set = CardSet.findByCode(setCode);
        if (set == null || getPrintings(set).stream()
                .noneMatch(printing -> printing.collectorNumber().equals(collectorNumber))) {
            return null;
        }
        return findByCollectorNumber(set, collectorNumber).createCard();
    }

    /**
     * Creates the card of a printing an effect names (set code and collector number), so the card carries that
     * printing's identity (its art, the set its tokens come from). The card is still mutable.
     *
     * @throws IllegalStateException if that printing is not implemented
     */
    default Card createCard(DraftFromSpellbookEffect.SpellbookCard printing) {
        Card card = createCardIfImplemented(printing.setCode(), printing.collectorNumber());
        if (card == null) {
            throw new IllegalStateException("No implemented printing " + printing.setCode() + " "
                    + printing.collectorNumber());
        }
        return card;
    }

    /**
     * Every implemented card class once, by its first printing in {@link CardSet} order, with the
     * oracle data a card built from that printing would carry. No card is built. Random-card effects
     * and card-name prompts search the whole catalog this way, because building every card to read
     * its characteristics loads every card class.
     *
     * <p>A class whose oracle data is not registered yet has it loaded first, from the set a built
     * card would load it from, wherever the load mode lets construction do that. A class without
     * oracle data has no entry.
     */
    List<CatalogEntry> getEntries();

    /** The printings of the {@link #getEntries() entries} whose oracle data matches. */
    default List<CardPrinting> findPrintings(Predicate<OracleData> predicate) {
        return getEntries().stream()
                .filter(entry -> predicate.test(entry.oracle()))
                .map(CatalogEntry::printing)
                .toList();
    }

    /**
     * The first registered printing of the card with this name, among cards whose oracle data is
     * already loaded; empty for any other name. Every card in a game had its oracle data loaded
     * when it was built, so this names the printing of a card that carries none of its own — a
     * runtime copy, or a card built straight from its class. Loads no set and builds no card.
     */
    Optional<CardPrinting> findLoadedPrintingByName(String cardName);

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
