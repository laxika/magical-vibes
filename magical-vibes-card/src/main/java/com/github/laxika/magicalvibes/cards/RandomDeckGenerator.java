package com.github.laxika.magicalvibes.cards;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Builds a randomly generated 40-card deck (1–3 colors, up to 4 copies per card, basic lands
 * distributed evenly across the deck's colors) from every implemented printing. Used by the
 * "All Random" game mode and the AI fuzz tests.
 *
 * <p>The card pool is built lazily on first use and cached for the lifetime of this instance. It
 * was previously cached in statics for the lifetime of the JVM, which silently tied every caller to
 * whichever {@link CardCatalog} happened to warm it first; the cache now lives and dies with the
 * catalog it was derived from.
 */
public final class RandomDeckGenerator {

    /** Sentinel deck id resolved by the engine to a freshly generated random deck. */
    public static final String RANDOM_DECK_ID = "random";

    public static final int DECK_SIZE = 40;
    public static final int LAND_COUNT = 18;
    public static final int SPELL_COUNT = DECK_SIZE - LAND_COUNT;

    /**
     * Minimum fraction of a set's card pool that must be implemented before the set is offered as a
     * source for set-restricted random decks. Sparsely-implemented sets would otherwise produce
     * repetitive decks padded with a handful of cards, so they are left to the "All sets" option.
     */
    public static final double MIN_SET_IMPLEMENTED_FRACTION = 0.80;

    private final Map<CardColor, CardPrinting> basicLandPrintings = new EnumMap<>(CardColor.class);
    private final CardCatalog catalog;
    private List<CardPrinting> allNonLandPrintings;

    public RandomDeckGenerator(CardCatalog catalog) {
        this.catalog = catalog;
    }

    public record GeneratedDeck(Set<CardColor> colors, List<Card> cards) {
    }

    public GeneratedDeck generate(Random rng) {
        return generate(rng, null);
    }

    /**
     * Builds a random deck drawn only from the set with the given code (e.g. {@code "AKH"}), or from
     * every implemented set when {@code setCode} is {@code null} ("All sets"). A single small set may
     * not have four distinct printings for the chosen colors; the deck is still filled to
     * {@link #DECK_SIZE} by relaxing the copy cap and, if no card matches the chosen colors at all,
     * by drawing from the whole set pool.
     *
     * @throws IllegalArgumentException if {@code setCode} names a set with no random-deckable card
     */
    public GeneratedDeck generate(Random rng, String setCode) {
        initializeCardPool();
        List<CardPrinting> pool = poolForSet(setCode);
        if (pool.isEmpty()) {
            throw new IllegalArgumentException("No random-deckable cards in set: " + setCode);
        }
        Set<CardColor> colors = pickDeckColors(rng);
        return new GeneratedDeck(colors, buildDeck(colors, pool, rng));
    }

    /** Whether {@code setCode} has at least one card the random generator can build a deck from. */
    public boolean hasDeckableCards(String setCode) {
        initializeCardPool();
        return !poolForSet(setCode).isEmpty();
    }

    /**
     * Whether the set is complete enough (≥ {@link #MIN_SET_IMPLEMENTED_FRACTION} of its card pool
     * implemented) to be offered as a source for set-restricted random decks.
     */
    public boolean isSetRandomEligible(CardSet set) {
        return catalog.getImplementedFraction(set) >= MIN_SET_IMPLEMENTED_FRACTION;
    }

    private List<CardPrinting> poolForSet(String setCode) {
        if (setCode == null) {
            return allNonLandPrintings;
        }
        List<CardPrinting> filtered = new ArrayList<>();
        for (CardPrinting printing : allNonLandPrintings) {
            if (setCode.equals(printing.setCode())) {
                filtered.add(printing);
            }
        }
        return filtered;
    }

    private Set<CardColor> pickDeckColors(Random rng) {
        // 20% mono-color, 60% two-color, 20% three-color
        int roll = rng.nextInt(10);
        int colorCount = roll < 2 ? 1 : roll < 8 ? 2 : 3;
        List<CardColor> all = new ArrayList<>(List.of(CardColor.values()));
        Collections.shuffle(all, rng);
        return EnumSet.copyOf(all.subList(0, colorCount));
    }

    private List<Card> buildDeck(Set<CardColor> deckColors, List<CardPrinting> pool, Random rng) {
        List<CardPrinting> playable = new ArrayList<>();
        for (CardPrinting printing : pool) {
            Card sample = printing.createCard();
            if (deckColors.containsAll(sample.getColors())) {
                playable.add(printing);
            }
        }
        // A small single-set pool may hold no card matching the randomly chosen colors; fall back
        // to the whole set pool so the deck can still be filled.
        if (playable.isEmpty()) {
            playable = new ArrayList<>(pool);
        }

        // Sample with replacement, preferring up to 4 copies per printing, so same-card
        // interactions (multiple copies in play, in the graveyard, legend rule) get exercised too.
        List<Card> deck = new ArrayList<>();
        List<CardPrinting> capped = new ArrayList<>(playable);
        Map<CardPrinting, Integer> copiesUsed = new HashMap<>();
        while (deck.size() < SPELL_COUNT && !capped.isEmpty()) {
            int idx = rng.nextInt(capped.size());
            CardPrinting printing = capped.get(idx);
            deck.add(printing.createCard());
            if (copiesUsed.merge(printing, 1, Integer::sum) >= 4) {
                capped.remove(idx);
            }
        }
        // A set-restricted pool can run out of distinct printings for the chosen colors before the
        // deck is full; top up from the same playable pool, allowing extra copies, so the deck still
        // reaches DECK_SIZE. With "All sets" the cap is never exhausted, so this loop never runs.
        while (deck.size() < SPELL_COUNT) {
            deck.add(playable.get(rng.nextInt(playable.size())).createCard());
        }

        // Distribute lands as evenly as possible across the deck's colors
        List<CardColor> colors = new ArrayList<>(deckColors);
        for (int i = 0; i < LAND_COUNT; i++) {
            deck.add(basicLandPrintings.get(colors.get(i % colors.size())).createCard());
        }

        return deck;
    }

    private synchronized void initializeCardPool() {
        if (allNonLandPrintings != null) {
            return;
        }

        basicLandPrintings.put(CardColor.WHITE, catalog.findByCollectorNumber(CardSet.SET_SOM, "230"));
        basicLandPrintings.put(CardColor.BLUE, catalog.findByCollectorNumber(CardSet.SET_SOM, "234"));
        basicLandPrintings.put(CardColor.BLACK, catalog.findByCollectorNumber(CardSet.SET_SOM, "238"));
        basicLandPrintings.put(CardColor.RED, catalog.findByCollectorNumber(CardSet.SET_SOM, "242"));
        basicLandPrintings.put(CardColor.GREEN, catalog.findByCollectorNumber(CardSet.SET_SOM, "246"));

        List<CardPrinting> printings = new ArrayList<>();
        for (CardSet set : CardSet.values()) {
            for (CardPrinting printing : catalog.getPrintings(set)) {
                Card sample = printing.createCard();
                if (!sample.hasType(CardType.LAND) && sample.getManaCost() != null) {
                    printings.add(printing);
                }
            }
        }
        allNonLandPrintings = printings;
    }
}
