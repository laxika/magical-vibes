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
 * <p>The card pool is scanned lazily on first use and cached for the lifetime of the JVM;
 * the Scryfall oracle registry must be loaded before the first call.
 */
public final class RandomDeckGenerator {

    /** Sentinel deck id resolved by the engine to a freshly generated random deck. */
    public static final String RANDOM_DECK_ID = "random";

    public static final int DECK_SIZE = 40;
    public static final int LAND_COUNT = 18;
    public static final int SPELL_COUNT = DECK_SIZE - LAND_COUNT;

    private static final Map<CardColor, CardPrinting> BASIC_LAND_PRINTINGS = new EnumMap<>(CardColor.class);
    private static List<CardPrinting> allNonLandPrintings;

    private RandomDeckGenerator() {
    }

    public record GeneratedDeck(Set<CardColor> colors, List<Card> cards) {
    }

    public static GeneratedDeck generate(Random rng) {
        initializeCardPool();
        Set<CardColor> colors = pickDeckColors(rng);
        return new GeneratedDeck(colors, buildDeck(colors, rng));
    }

    private static Set<CardColor> pickDeckColors(Random rng) {
        // 20% mono-color, 60% two-color, 20% three-color
        int roll = rng.nextInt(10);
        int colorCount = roll < 2 ? 1 : roll < 8 ? 2 : 3;
        List<CardColor> all = new ArrayList<>(List.of(CardColor.values()));
        Collections.shuffle(all, rng);
        return EnumSet.copyOf(all.subList(0, colorCount));
    }

    private static List<Card> buildDeck(Set<CardColor> deckColors, Random rng) {
        List<CardPrinting> playable = new ArrayList<>();
        for (CardPrinting printing : allNonLandPrintings) {
            Card sample = printing.createCard();
            if (deckColors.containsAll(sample.getColors())) {
                playable.add(printing);
            }
        }

        // Sample with replacement, up to 4 copies per printing, so same-card
        // interactions (multiple copies in play, in the graveyard, legend rule)
        // get exercised too.
        List<Card> deck = new ArrayList<>();
        Map<CardPrinting, Integer> copiesUsed = new HashMap<>();
        while (deck.size() < SPELL_COUNT && !playable.isEmpty()) {
            int idx = rng.nextInt(playable.size());
            CardPrinting printing = playable.get(idx);
            deck.add(printing.createCard());
            if (copiesUsed.merge(printing, 1, Integer::sum) >= 4) {
                playable.remove(idx);
            }
        }

        // Distribute lands as evenly as possible across the deck's colors
        List<CardColor> colors = new ArrayList<>(deckColors);
        for (int i = 0; i < LAND_COUNT; i++) {
            deck.add(BASIC_LAND_PRINTINGS.get(colors.get(i % colors.size())).createCard());
        }

        return deck;
    }

    private static synchronized void initializeCardPool() {
        if (allNonLandPrintings != null) {
            return;
        }

        BASIC_LAND_PRINTINGS.put(CardColor.WHITE, CardSet.SET_SOM.findByCollectorNumber("230"));
        BASIC_LAND_PRINTINGS.put(CardColor.BLUE, CardSet.SET_SOM.findByCollectorNumber("234"));
        BASIC_LAND_PRINTINGS.put(CardColor.BLACK, CardSet.SET_SOM.findByCollectorNumber("238"));
        BASIC_LAND_PRINTINGS.put(CardColor.RED, CardSet.SET_SOM.findByCollectorNumber("242"));
        BASIC_LAND_PRINTINGS.put(CardColor.GREEN, CardSet.SET_SOM.findByCollectorNumber("246"));

        List<CardPrinting> printings = new ArrayList<>();
        for (CardSet set : CardSet.values()) {
            for (CardPrinting printing : set.getPrintings()) {
                Card sample = printing.createCard();
                if (!sample.hasType(CardType.LAND) && sample.getManaCost() != null) {
                    printings.add(printing);
                }
            }
        }
        allNonLandPrintings = printings;
    }
}
